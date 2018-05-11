package org.jeson.reinforce.viewmodel;

import org.jeson.reinforce.model.ReinforceModel;
import org.jeson.reinforce.resource.Resources;
import org.jeson.reinforce.util.FileUtil;
import org.jeson.reinforce.util.Util;
import org.jeson.reinforce.view.frame.ReinforceFrame;

public class ReinforceViewModel implements ReinforceFrame.OnRequestAction, ReinforceModel.OnProcessFinishedListener, FileUtil.ReadLineAction {
    private ReinforceFrame mFrame;
    private ReinforceModel mModel;

    public ReinforceViewModel() {
        mModel = new ReinforceModel();
        mFrame = new ReinforceFrame();
        prepare();
    }

    private void prepare() {
        mFrame.setOnRequestAction(this);
        mModel.setOnProcessFinishedListener(this);
    }

    public void show() {
        show(null);
    }

    public void show(String[] args) {
        mFrame.setVisible(true);
        if (args != null) {
            if (args.length == 1) {
                performRequest(args[0], null);
            } else if (args.length > 1) {
                performRequest(args[0], args[1]);
            }
        }
    }

    public void performRequest(String srcApkPath, String dstApkPath) {
        mFrame.updateApkPath(srcApkPath, dstApkPath);
        onRequest(srcApkPath, dstApkPath);
    }

    @Override
    public void onRequest(String srcApkPath, String dstApkPath) {
        mFrame.clearLog();
        mFrame.setMemAndTimeUsed(0, 0);
        if (isRunning()) {
            interruptWork();
            mFrame.setActionDescription(Resources.RESOURCES.getString("startAction"));
        } else {
            if (Util.isEmptyText(srcApkPath)) {
                mFrame.appendLog(Resources.RESOURCES.getString("srcApkPathInvalid"));
                return;
            }
            mFrame.setActionDescription(Resources.RESOURCES.getString("stopAction"));
            startWork(srcApkPath, dstApkPath);
        }
    }

    private synchronized void interruptWork() {
        if (isRunning()) {
            mModel.interruptWork();
            FileUtil.closeIO(mModel.getInputStream());
            FileUtil.closeIO(mModel.getErrorStream());
        }
    }

    private synchronized void startWork(String srcApkPath, String dstApkPath) {
        if (!isRunning()) {
            mModel.start(srcApkPath, dstApkPath);
            FileUtil.readLineInWorkThread(mModel.getInputStream(), this);
            FileUtil.readLineInWorkThread(mModel.getErrorStream(), this);
        }
    }

    private boolean isRunning() {
        return mModel.isRunning();
    }

    @Override
    public void onFinished(int exitValue, long mem, long time) {
        mFrame.setMemAndTimeUsed(mem, time);
        mFrame.appendLog("Reinforce process finished with exit code " + exitValue);
        mFrame.setActionDescription(Resources.RESOURCES.getString("startAction"));
    }

    @Override
    public void onReadLine(String line) {
        mFrame.appendLog(line);
    }
}
