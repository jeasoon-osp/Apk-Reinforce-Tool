package org.jeson.reinforcetool.processor;

import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.file.ApkFile;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZipalignProcessor implements ErrorCode {

    private final String zipalignExecutorPath;

    public ZipalignProcessor() {
        this(null);
    }

    public ZipalignProcessor(String zipalignExecutorPath) {
        if (Util.isEmptyText(zipalignExecutorPath)) {
            this.zipalignExecutorPath = loadDefaultZipalignExecutor();
        } else {
            this.zipalignExecutorPath = zipalignExecutorPath;
        }
    }

    public int process(String srcApkPath, String dstApkPath) {
        ApkFile srcApkFile = new ApkFile(srcApkPath);
        if (Util.isEmptyText(zipalignExecutorPath)) {
            return FAILED_ZIPALIGN_EXECUTOR_INVALID;
        }
        if (!srcApkFile.exists()) {
            return FAILED_ZIPALIGN_SOURCE_APK_INVALID;
        }
        if (Util.isEmptyText(dstApkPath)) {
            if (srcApkPath.endsWith(".apk")) {
                dstApkPath = srcApkPath.substring(0, srcApkPath.lastIndexOf(".apk")) + "-ZipAligned.apk";
            } else {
                dstApkPath = srcApkPath + "-ZipAligned.apk";
            }
        }
        ApkFile dstApkFile = new ApkFile(dstApkPath);
        File tmpFile = new File(dstApkFile.path() + "_.tmp");
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String[] cmd = new String[]{zipalignExecutorPath, "-f", "-p", "4", srcApkFile.path(), tmpFile.getAbsolutePath()};
            builder.command(cmd);
            Logger.DEFAULT.info("zipalign apk file cmd: " + Util.cmd(cmd));
            process = builder.start();
            int exitValue = process.waitFor();
            dstApkFile.file().delete();
            tmpFile.renameTo(dstApkFile.file());
            if (exitValue == 0) {
                return SUCCESS;
            } else {
                Logger.DEFAULT.error("zipalign apk failed, exit value is " + exitValue);
                return FAILED_ZIPALIGN_APK_UNKNOWN_ERROR;
            }
        } catch (Exception e) {
            Logger.DEFAULT.error("zipalign apk failed!", e);
            return FAILED_ZIPALIGN_APK_UNKNOWN_ERROR;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private String loadDefaultZipalignExecutor() {
        File tempDir = FileUtil.getTempFile("tools");
        List<String> resourceExecutorPath = new ArrayList<>();
        List<String> dstExecutorRealPath = new ArrayList<>();
        String executorPath = "/apk/zipalign/win/zipalign.exe";
        switch (Util.getOsType()) {
            case Util.OS_WIN:
                executorPath = "/apk/zipalign/win/zipalign.exe";
                break;
            case Util.OS_LINUX:
                executorPath = "/apk/zipalign/linux/zipalign";
                resourceExecutorPath.add("/apk/zipalign/unixlib/libc++.so");
                break;
            case Util.OS_MAC:
                executorPath = "/apk/zipalign/mac/zipalign";
                resourceExecutorPath.add("/apk/zipalign/unixlib/libc++.so");
                break;
        }
        resourceExecutorPath.add(executorPath);
        boolean isSuccess = true;
        for (String path : resourceExecutorPath) {
            String realPath = getDstRealPath(tempDir, path);
            dstExecutorRealPath.add(realPath);
            isSuccess &= FileUtil.cloneFromResources(path, realPath, false);
        }
        if (Util.getOsType() != Util.OS_WIN) {
            try {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                for (String path : dstExecutorRealPath) {
                    Files.setPosixFilePermissions(new File(path).toPath(), permissions);
                }
            } catch (IOException e) {
                isSuccess = false;
                Logger.DEFAULT.error(e.getMessage(), e);
            }
        }
        if (!isSuccess) {
            Logger.DEFAULT.error("load default zipalign failed!");
        }
        System.gc();
        return isSuccess ? getDstRealPath(tempDir, executorPath) : null;
    }

    private String getDstRealPath(File dir, String path) {
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.DEFAULT.error("copy file from resources" + path);
            return null;
        }
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return dir.getAbsolutePath() + File.separator + fileName;
    }

}
