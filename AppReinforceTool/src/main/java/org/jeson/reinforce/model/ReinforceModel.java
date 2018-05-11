package org.jeson.reinforce.model;

import org.jeson.reinforce.util.FileUtil;
import org.jeson.reinforce.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class ReinforceModel {

    private static final String PATH_REINFORCE_TOOL_JAR_IN_RESOURCE = "/tool/ApkReinforceTool.jar";
    private static final String PATH_REINFORCE_TOOL_JAR_IN_TEMP_DIR = FileUtil.getTempFile("ApkReinforceTool/tool/ApkReinforceTool.jar").getAbsolutePath();

    static {
        FileUtil.cloneFromResources(PATH_REINFORCE_TOOL_JAR_IN_RESOURCE, PATH_REINFORCE_TOOL_JAR_IN_TEMP_DIR, false);
    }

    private Process mRunningProcess;
    private Thread mRunningThread;
    private OnProcessFinishedListener mOnProcessFinishedListener;

    public synchronized void start(String srcApkPath, String dstApkPath) {
        if (isRunning()) {
            interruptWork();
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(buildArgs(srcApkPath, dstApkPath));
        try {
            long startTime = System.currentTimeMillis();
            Process process = pb.start();
            mRunningProcess = process;
            mRunningThread = new Thread() {
                @Override
                public void run() {
                    try {
                        int exitValue = process.waitFor();
                        long endTime = System.currentTimeMillis();
                        if (mOnProcessFinishedListener != null) {
                            mOnProcessFinishedListener.onFinished(exitValue, Runtime.getRuntime().totalMemory() / 1024 / 1024, endTime - startTime);
                        }
                    } catch (InterruptedException e) {
                    } finally {
                        try {
                            if (mRunningProcess != null) {
                                mRunningProcess.destroyForcibly();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            mRunningThread = null;
                            mRunningProcess = null;
                        }
                    }
                }
            };
            mRunningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String[] buildArgs(String srcApkPath, String dstApkPath) {
        String[] args;
        if (Util.isEmptyText(dstApkPath)) {
            args = new String[5];
        } else {
            args = new String[7];
        }
        args[0] = "java";
        args[1] = "-jar";
        args[2] = PATH_REINFORCE_TOOL_JAR_IN_TEMP_DIR;
        args[3] = "-i";
        args[4] = srcApkPath;
        if (!Util.isEmptyText(dstApkPath)) {
            args[5] = "-o";
            args[6] = dstApkPath;
        }
        return args;
    }

    public InputStream getInputStream() {
        if (isRunning()) {
            return mRunningProcess.getInputStream();
        }
        return null;
    }

    public InputStream getErrorStream() {
        if (isRunning()) {
            return mRunningProcess.getErrorStream();
        }
        return null;
    }

    public synchronized void interruptWork() {
        if (isRunning()) {
            try {
                if (mRunningProcess != null) {
                    mRunningProcess.destroyForcibly();
                }
                if (mRunningThread != null) {
                    mRunningThread.interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mRunningProcess = null;
                mRunningThread = null;
            }
        }
    }

    public synchronized boolean isRunning() {
        return mRunningProcess != null || mRunningThread != null;
    }

    public void setOnProcessFinishedListener(OnProcessFinishedListener onProcessFinishedListener) {
        mOnProcessFinishedListener = onProcessFinishedListener;
    }

    public interface OnProcessFinishedListener {
        void onFinished(int exitValue, long mem, long time);
    }

}
