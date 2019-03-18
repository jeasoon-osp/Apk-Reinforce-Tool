package org.jeson.reinforcetool.processor;

import brut.apktool.Main;
import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.file.ApkFile;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.File;
import org.jeson.reinforcetool.file.ManifestFile;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.util.FileUtil;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApkProcessor implements ErrorCode {

    private final File workspaceDir;

    private final File tmpOutDir;

    public ApkProcessor(String workspacePath) {
        workspaceDir = new File(workspacePath);
        tmpOutDir = new File(workspaceDir.path() + java.io.File.separator + "__out");
    }

    public boolean decodeApk(String srcApkPath) {
        try {
            ApkFile srcApkFile = new ApkFile(srcApkPath);
            cleanWorkspace(srcApkFile.path());
            boolean isSuccess = tmpOutDir.file().mkdirs() && srcApkFile.exists();
            if (isSuccess) {
                Main.main(buildDecodeArgs(srcApkFile.path(), tmpOutDir.path()));
            }
            return isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String[] buildDecodeArgs(String apkPath, String outDir) {
        return new String[]{"d", "-f", "-s", "-o", outDir, apkPath};
    }

    public boolean encodeApk(String dstApkPath) {
        try {
            ApkFile dstApkFile = new ApkFile(dstApkPath);
            FileUtil.delete(dstApkFile);
            dstApkFile.mkParentDir();
            Main.main(buildEncodeArgs(tmpOutDir.path(), dstApkFile.path()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String[] buildEncodeArgs(String appDir, String outApkPath) {
        return new String[]{"b", "-f", "--use-aapt2", "-o", outApkPath, appDir};
    }

    public File getAppOutDir() {
        return tmpOutDir;
    }

    public boolean cleanWorkspace(String... exclude) {
        return FileUtil.delete(workspaceDir, exclude);
    }

    public ManifestFile manifestFile() {
        return new ManifestFile(tmpOutDir.path() + java.io.File.separator + "AndroidManifest.xml");
    }

    public List<DexFile> listWorkspaceDex() {
        List<DexFile> dexFiles = new ArrayList<>();
        if (!tmpOutDir.exists()) {
            Logger.DEFAULT.error("workspace is not a dir or not exist!");
            return dexFiles;
        }
        java.io.File[] files = tmpOutDir.file().listFiles(new FileFilter() {
            @Override
            public boolean accept(java.io.File pathname) {
                return pathname.isFile() && pathname.getName().matches("classes\\d*\\.dex");
            }
        });
        if (files != null) {
            for (java.io.File file : files) {
                DexFile dexFile = new DexFile(file.getAbsolutePath());
                if (dexFile.checkMagicNumber()) {
                    dexFiles.add(dexFile);
                }
            }
        }
        Collections.sort(dexFiles, new Comparator<DexFile>() {
            @Override
            public int compare(DexFile o1, DexFile o2) {
                return o1.name().compareToIgnoreCase(o2.name());
            }
        });
        return dexFiles;
    }


}
