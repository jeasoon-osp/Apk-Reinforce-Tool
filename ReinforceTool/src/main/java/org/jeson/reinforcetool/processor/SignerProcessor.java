package org.jeson.reinforcetool.processor;

import com.android.apksigner.ApkSignerTool;
import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.file.ApkFile;
import org.jeson.reinforcetool.file.File;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SignerProcessor implements ErrorCode {

    private final Options options;

    public SignerProcessor() {
        this(Options.DEFAULT);
    }

    public SignerProcessor(Properties properties) {
        this(properties == null ? Options.DEFAULT : Options.build(properties));
    }

    public SignerProcessor(String keyStorePath, String keyStoreAlias, String keyStorePass, String keyPass) {
        this(new Options(keyStorePath, keyStoreAlias, keyStorePass, keyPass));
    }

    public SignerProcessor(Options options) {
        this.options = options;
    }

    public int sign(String srcApkPath, String dstApkPath) {
        if (options == null) {
            return FAILED_SIGNER_PROCESSOR_ARGS_INVALID;
        }
        if (Util.isEmptyText(srcApkPath)) {
            return FAILED_SIGNER_NULL_SOURCE_APK_ERROR;
        }
        ApkFile srcApkFile = new ApkFile(srcApkPath);
        if (!srcApkFile.exists()) {
            return FAILED_SIGNER_NULL_SOURCE_APK_ERROR;
        }
        if (Util.isEmptyText(dstApkPath)) {
            if (srcApkPath.endsWith(".apk")) {
                dstApkPath = srcApkPath.substring(0, srcApkPath.lastIndexOf(".apk")) + "-Signed.apk";
            } else {
                dstApkPath = srcApkPath + "-Signed.apk";
            }
        }
        ApkFile dstApkFile = new ApkFile(dstApkPath);
        java.io.File parentFile = dstApkFile.file().getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            return FAILED_SIGNER_NULL_DST_APK_ERROR;
        }
        try {
            java.io.File tmpFile = new java.io.File(dstApkPath + "_.tmp");
            String[] cmd = buildArgs(options, srcApkPath, tmpFile.getAbsolutePath());
            ApkSignerTool.main(cmd);
            dstApkFile.file().delete();
            tmpFile.renameTo(dstApkFile.file());
            Logger.DEFAULT.info("sign apk file cmd: " + Util.cmd(cmd));
            return SUCCESS;
        } catch (Exception e) {
            Logger.DEFAULT.error(e.getMessage(), e);
            return FAILED_SIGNER_SIGN_APK_UNKNOWN_ERROR;
        }
    }

    private String[] buildArgs(Options options, String srcApkPath, String dstApkPath) {
        String[] args = new String[15];
        args[0] = "sign";
        args[1] = Options.KEY_STORE_PATH;
        args[2] = options.keyStorePath;
        args[3] = Options.KEY_STORE_ALIAS;
        args[4] = options.keyStoreAlias;
        args[5] = Options.KEY_STORE_PASS;
        args[6] = "pass:" + options.keyStorePass;
        args[7] = Options.KEY_PASS;
        args[8] = "pass:" + options.keyPass;
        args[9] = "--min-sdk-version";
        args[10] = "14";
        args[11] = "--out";
        args[12] = dstApkPath;
        args[13] = "--in";
        args[14] = srcApkPath;
        return args;
    }

    public static class Options {

        public static final String KEY_STORE_PACKAGE = "--ks-package";
        public static final String KEY_STORE_VERSION = "--ks-version";
        public static final String KEY_STORE_PATH = "--ks";
        public static final String KEY_STORE_ALIAS = "--ks-key-alias";
        public static final String KEY_STORE_PASS = "--ks-pass";
        public static final String KEY_PASS = "--key-pass";

        public static final Options DEFAULT = loadDefault();

        private static Options loadDefault() {
            InputStream defaultInput = null;
            try {
                defaultInput = Options.class.getResourceAsStream("/apk/signer/signer.properties");
                if (defaultInput == null) {
                    throw new RuntimeException("can't load signer.properties form /apk/signer/signer.properties, can't use default signer!");
                }
                Properties properties = new Properties();
                properties.load(defaultInput);
                return build(properties);
            } catch (Exception e) {
                Logger.DEFAULT.error(e.getMessage(), e);
                return null;
            } finally {
                FileUtil.closeIO(defaultInput);
            }
        }

        public static Options build(Properties properties) {
            InputStream keyStoreInput = null;
            OutputStream keyStoreOutput = null;
            try {
                String keyStorePackage = properties.getProperty(KEY_STORE_PACKAGE, "default");
                String keyStoreVersion = properties.getProperty(KEY_STORE_VERSION, "1.0");
                String keyStoreAlias = properties.getProperty(KEY_STORE_ALIAS);
                String keyStorePass = properties.getProperty(KEY_STORE_PASS);
                String keyStorePath = properties.getProperty(KEY_STORE_PATH);
                String keyPass = properties.getProperty(KEY_PASS);

                if (Util.isEmptyText(keyStorePath)) {
                    keyStoreInput = Options.class.getResourceAsStream("/apk/signer/signer.jks");
                } else {
                    keyStoreInput = new FileInputStream(keyStorePath);
                }
                String tempDirPath = FileUtil.getTempDir().getAbsolutePath();
                File tempKeyStoreFile = new File(tempDirPath + java.io.File.separator + "signer" + java.io.File.separator + keyStorePackage + java.io.File.separator + keyStoreVersion + ".jks");
                if (!tempKeyStoreFile.exists() || keyStoreInput.available() != tempKeyStoreFile.size()) {
                    keyStoreOutput = tempKeyStoreFile.create();
                    if (keyStoreOutput == null) {
                        throw new RuntimeException("can't create temp key store form " + tempDirPath + ", can't use default signer!");
                    }
                    boolean isSuccess = FileUtil.readAndWriteData(keyStoreInput, keyStoreOutput);
                    if (!isSuccess) {
                        throw new RuntimeException("copy default key store failed form " + tempDirPath + ", can't use default signer!");
                    }
                }
                return new Options(tempKeyStoreFile.path(), keyStoreAlias, keyStorePass, keyPass);
            } catch (Exception e) {
                Logger.DEFAULT.error(e.getMessage(), e);
                return null;
            } finally {
                FileUtil.closeIO(keyStoreInput);
                FileUtil.closeIO(keyStoreOutput);
            }
        }

        private final String keyStorePath;
        private final String keyStoreAlias;
        private final String keyStorePass;
        private final String keyPass;

        public Options(String keyStorePath, String keyStoreAlias, String keyStorePass, String keyPass) {
            if (Util.isEmptyText(keyStorePath)) {
                throw new RuntimeException("key store path is empty!");
            }
            if (Util.isEmptyText(keyStoreAlias)) {
                throw new RuntimeException("key store alias is empty!");
            }
            if (Util.isEmptyText(keyStorePass)) {
                throw new RuntimeException("key store password is empty!");
            }
            if (Util.isEmptyText(keyPass)) {
                throw new RuntimeException("key password is empty!");
            }
            this.keyStorePath = keyStorePath;
            this.keyStoreAlias = keyStoreAlias;
            this.keyStorePass = keyStorePass;
            this.keyPass = keyPass;
        }

    }
}
