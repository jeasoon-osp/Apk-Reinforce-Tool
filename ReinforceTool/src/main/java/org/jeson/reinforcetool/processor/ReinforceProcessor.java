package org.jeson.reinforcetool.processor;

import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.ManifestFile;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.ReflectUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReinforceProcessor implements ErrorCode {

    private static final String LIB_RESOURCE = "/lib/" + Util.getOsArch();

    private final Options options;
    private Map<String, Properties> mPropertiesMap = new HashMap<>();

    public ReinforceProcessor(String srcApkPath) {
        this(srcApkPath, null);
    }

    public ReinforceProcessor(String srcApkPath, String dstApkPath) {
        this(Options.build(srcApkPath, dstApkPath));
    }

    public ReinforceProcessor setSignerProperties(Properties properties) {
        mPropertiesMap.put(SignerProcessor.class.getSimpleName(), properties);
        return this;
    }

    public ReinforceProcessor(Options options) {
        if (!options.isValid()) {
            throw new RuntimeException("Reinforce processor option is invalid");
        }
        if (Util.isEmptyText(options.dstApkPath)) {
            if (options.srcApkPath.endsWith(".apk")) {
                options.dstApkPath = options.srcApkPath.substring(0, options.srcApkPath.lastIndexOf(".apk")) + options.suffixReinforced + options.suffixSigned + options.suffixZipAligned + ".apk";
            } else {
                options.dstApkPath = options.srcApkPath + options.suffixReinforced + options.suffixSigned + options.suffixZipAligned + ".apk";
            }
        }
        if (Util.isEmptyText(options.workspaceDirPath)) {
            if (options.srcApkPath.endsWith(".apk")) {
                options.workspaceDirPath = options.srcApkPath.substring(0, options.srcApkPath.lastIndexOf(".apk")) + options.suffixWorkspace;
            } else {
                options.workspaceDirPath = options.srcApkPath + options.suffixWorkspace;
            }
        }
        options.dstDexPath = options.workspaceDirPath + File.separator + "classes.dex";
        this.options = options.clone();
    }

    public int process() {
        // 准备加密解密库
        prepareLibrary();
        // 清理工作空间
        cleanWorkspace();
        // 检查参数是否有效
        if (!checkOptionValid()) {
            Logger.DEFAULT.error("reinforce processor args is invalid!");
            return FAILED_REINFORCE_ARGS_INVALID;
        }
        // 解压apk
        ApkProcessor apkProcessor = new ApkProcessor(options.workspaceDirPath);
        boolean isSuccess = apkProcessor.decodeApk(options.srcApkPath);
        if (!isSuccess) {
            cleanWorkspace();
            Logger.DEFAULT.error("decode apk failed!");
            return FAILED_APK_DECODE;
        }
        // 给清单文件添加指定的application
        ManifestFile axmlFile = apkProcessor.manifestFile();
        String dstAppClassName = new ManifestProcessor().process(axmlFile, axmlFile);
        //处理dex文件
        List<DexFile> dexFilesToAppend = apkProcessor.listWorkspaceDex();
        isSuccess = new DexPretendProcessor().process(apkProcessor.getAppOutDir(), dstAppClassName, dexFilesToAppend);
        if (!isSuccess) {
            cleanWorkspace();
            Logger.DEFAULT.error("merge dex file failed!");
            return FAILED_DEX_PRETEND;
        }
        // 添加lib库到apk工作空间lib目录下
        if (!Util.isEmptyText(options.libDir)) {
            cloneSrcLibToWorkspaceLib(options.libDir,apkProcessor.getAppOutDir().path());
        }
        // 添加resource预置的lib库到apk工作空间lib目录下
        cloneResourceLibToWorkspaceLib(apkProcessor.getAppOutDir().path());
        // 回编
        isSuccess = apkProcessor.encodeApk(options.dstApkPath);
        if (!isSuccess) {
            cleanWorkspace();
            Logger.DEFAULT.error("encode apk failed!");
            return FAILED_APK_ENCODE;
        }
        // zip对齐优化
        int result = new ZipalignProcessor().process(options.dstApkPath, options.dstApkPath);
        if (result != SUCCESS) {
            cleanWorkspace();
            Logger.DEFAULT.error("zipalign apk failed!");
            return result;
        }
        // apk签名
        result = new SignerProcessor(mPropertiesMap.get(SignerProcessor.class.getSimpleName())).sign(options.dstApkPath, options.dstApkPath);
        if (result != SUCCESS) {
            cleanWorkspace();
            Logger.DEFAULT.error("sign apk failed!");
            return result;
        }
        // 清理工作空间
        cleanWorkspace(false);
        return SUCCESS;
    }

    private void prepareLibrary() {
        File libDir = FileUtil.getTempDir();
        boolean success = FileUtil.cloneDirFromResource(LIB_RESOURCE, libDir.getAbsolutePath(), false);
        if (!success) {
            return;
        }
        File lib = new File(libDir, "lib/" + Util.getOsArch());
        File[] files = lib.listFiles();
        if (files == null) {
            return;
        }
        String libPath = System.getProperty("java.library.path");
        if (Util.isEmptyText(libPath)) {
            libPath = lib.getAbsolutePath();
        } else {
            if (Util.getOsType() == Util.OS_WIN) {
                libPath += ";" + lib.getAbsolutePath();
            } else {
                libPath += ":" + lib.getAbsolutePath();
            }
        }
        System.setProperty("java.library.path", libPath);
        ReflectUtil.fieldStatic(ClassLoader.class, "usr_paths", ReflectUtil.invokeStatic(ClassLoader.class, "initializePath", new Class[]{String.class}, new Object[]{"java.library.path"}));
    }

    private boolean cleanWorkspace() {
        return cleanWorkspace(true);
    }

    private boolean cleanWorkspace(boolean all) {
        if (all) {
            return FileUtil.delete(new File(options.workspaceDirPath), options.srcApkPath);
        } else {
            return FileUtil.delete(new File(options.workspaceDirPath), options.srcApkPath, options.dstApkPath);
        }
    }

    private boolean cloneResourceLibToWorkspaceLib(String dstLibDirPath) {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream("/apk/lib/lib.properties");
            if (inputStream == null) {
                Logger.DEFAULT.info("can't get file: /apk/lib/lib.properties, ignore!");
                return true;
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString().trim();
                String value = entry.getValue().toString().trim();
                if (Util.isEmptyText(key) || Util.isEmptyText(value)) {
                    continue;
                }
                String[] values = value.split(",");
                for (String libName : values) {
                    libName = libName.trim();
                    if (Util.isEmptyText(libName)) {
                        continue;
                    }
                    cloneResourceLib(dstLibDirPath, key, libName);
                }
            }
            properties.clear();
            return true;
        } catch (Exception e) {
            Logger.DEFAULT.error(e.getMessage(), e);
            return false;
        } finally {
            FileUtil.closeIO(inputStream);
            System.gc();
        }
    }

    private boolean cloneResourceLib(String workspaceDirPath, String key, String libName) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String libPath = "/apk/lib/" + key + "/lib" + libName + ".so";
            inputStream = getClass().getResourceAsStream(libPath);
            if (inputStream == null) {
                throw new RuntimeException("can't open file: " + libPath);
            }
            File dir = new File(workspaceDirPath + File.separator + "lib" + File.separator + key);
            if (!dir.exists()) {
                boolean isSuccess = dir.mkdirs();
                if (!isSuccess) {
                    throw new RuntimeException(dir.getAbsolutePath() + " this dir can't be created!");
                }
            } else if (dir.isFile()) {
                throw new RuntimeException(dir.getAbsolutePath() + " is a file!");
            }
            outputStream = new FileOutputStream(dir.getAbsolutePath() + File.separator + "lib" + libName + ".so");
            return FileUtil.readAndWriteData(inputStream, outputStream);
        } catch (Exception e) {
            Logger.DEFAULT.error(e.getMessage(), e);
            return false;
        } finally {
            FileUtil.closeIO(inputStream);
            FileUtil.closeIO(outputStream);
        }
    }


    private boolean cloneSrcLibToWorkspaceLib(String srcLibDir, String workspaceLibDir) {
        return FileUtil.cloneFile(srcLibDir, workspaceLibDir + File.separator + "lib");
    }

    private boolean checkOptionValid() {
        return !Util.isEmptyText(options.srcApkPath)
                && !Util.isEmptyText(options.dstApkPath)
                && !Util.isEmptyText(options.dstDexPath)
                && !Util.isEmptyText(options.workspaceDirPath);
    }


    public static class Options {

        public static final String SUFFIX_REINFORCED = "-Reinforced";

        public static final String SUFFIX_WORKSPACE = "-Workspace";

        public static final String SUFFIX_SIGNED = "-Signed";

        public static final String SUFFIX_ZIP_ALIGNED = "-ZipAligned";

        public String suffixReinforced = SUFFIX_REINFORCED;

        public String suffixWorkspace = SUFFIX_WORKSPACE;

        public String suffixSigned = SUFFIX_SIGNED;

        public String suffixZipAligned = SUFFIX_ZIP_ALIGNED;

        public String libDir;

        public String srcApkPath;

        public String dstApkPath;

        public String dstDexPath;

        public String workspaceDirPath;

        private Options() {
        }

        public static Options build(String srcApkPath, String dstApkPath) {
            Options options = new Options();
            options.srcApkPath = srcApkPath;
            options.dstApkPath = dstApkPath;
            return options;
        }

        protected Options clone() {
            Options options = new Options();
            options.suffixSigned = suffixSigned;
            options.suffixWorkspace = suffixWorkspace;
            options.suffixZipAligned = suffixZipAligned;
            options.suffixReinforced = suffixReinforced;
            options.libDir = libDir;
            options.srcApkPath = srcApkPath;
            options.dstApkPath = dstApkPath;
            options.dstDexPath = dstDexPath;
            options.workspaceDirPath = workspaceDirPath;
            return options;
        }

        private boolean isValid() {
            return !Util.isEmptyText(srcApkPath);
        }

    }

}
