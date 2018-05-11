package org.jeson.reinforce.view.frame;

import org.jeson.reinforce.resource.Resources;
import org.jeson.reinforce.util.FileUtil;
import org.jeson.reinforce.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReinforceFrame extends JFrame implements ActionListener {

    private static final String ID_TITLE = "appTitle";
    private static final String ID_HIN_SOURCE_APK = "hinSourceApk";
    private static final String ID_HIN_DST_APK = "hinDstApk";
    private static final String ID_ACTION_START = "startAction";
    private static final String ID_ACTION_FILE_CHOOSE = "fileChooser";
    private static final String ID_ACTION_SUMMARY = "actionSummary";
    private static final String ID_MINUTES = "minute";
    private static final String ID_SECONDS = "second";
    private static final String ID_RUNNING_LOG = "runningLog";
    private static final String ID_FILE_CHOOSER_TITLE = "fileChooserTitle";

    private static final String ICON_APP = "app_icon.png";

    private JTextArea taLog;
    private JButton btnAction;
    private JButton btnSrcFileChooser;
    private JButton btnDstFileChooser;
    private JTextField tfDstSrcApk;
    private JTextField tfInputSrcApk;
    private JLabel lbMemAndTimeUsed;
    private JFileChooser fcFileChooser;

    private OnRequestAction mOnRequestAction;

    public ReinforceFrame() {
        this(false);
    }

    public ReinforceFrame(boolean show) {
        initContent();
        initData();
        initView();
        setVisible(show);
    }

    private void initContent() {
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle(Resources.RESOURCES.getString(ID_TITLE));
        setIconImage(Resources.RESOURCES.getImage(ICON_APP));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initData() {
    }

    private void initView() {
        setSize(490, 490);

        fcFileChooser = new JFileChooser();
        fcFileChooser.setDialogTitle(Resources.RESOURCES.getString(ID_FILE_CHOOSER_TITLE));
        fcFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".apk") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return ".apk";
            }
        });

        tfInputSrcApk = new JTextField();
        tfInputSrcApk.grabFocus();
        tfInputSrcApk.setLocation(20, 50);
        tfInputSrcApk.setSize(410, 30);
        tfInputSrcApk.setMargin(new Insets(0, 5, 0, 5));

        tfDstSrcApk = new JTextField();
        tfDstSrcApk.setLocation(20, 120);
        tfDstSrcApk.setSize(410, 30);
        tfDstSrcApk.setMargin(new Insets(0, 5, 0, 5));

        JLabel hinSrcApkLabel = new JLabel(Resources.RESOURCES.getString(ID_HIN_SOURCE_APK));
        hinSrcApkLabel.setLabelFor(tfInputSrcApk);
        hinSrcApkLabel.setLocation(20, 20);
        hinSrcApkLabel.setSize(100, 30);

        JLabel hinDstApkLabel = new JLabel(Resources.RESOURCES.getString(ID_HIN_DST_APK));
        hinDstApkLabel.setLabelFor(tfDstSrcApk);
        hinDstApkLabel.setLocation(20, 90);
        hinDstApkLabel.setSize(100, 30);

        JLabel hinLogLabel = new JLabel(Resources.RESOURCES.getString(ID_RUNNING_LOG));
        hinLogLabel.setLocation(20, 210);
        hinLogLabel.setSize(100, 30);

        lbMemAndTimeUsed = new JLabel(Resources.RESOURCES.getString(ID_HIN_DST_APK));
        lbMemAndTimeUsed.setLocation(20, 170);
        lbMemAndTimeUsed.setSize(200, 40);
        lbMemAndTimeUsed.setText(getMemAndTime(0, 0));

        btnSrcFileChooser = new JButton(Resources.RESOURCES.getString(ID_ACTION_FILE_CHOOSE));
        btnSrcFileChooser.setLocation(430, 50);
        btnSrcFileChooser.setSize(30, 30);
        btnSrcFileChooser.setFocusPainted(false);
        btnSrcFileChooser.addActionListener(this);

        btnDstFileChooser = new JButton(Resources.RESOURCES.getString(ID_ACTION_FILE_CHOOSE));
        btnDstFileChooser.setLocation(430, 120);
        btnDstFileChooser.setSize(30, 30);
        btnDstFileChooser.setFocusPainted(false);
        btnDstFileChooser.addActionListener(this);

        btnAction = new JButton(Resources.RESOURCES.getString(ID_ACTION_START));
        btnAction.setLocation(360, 170);
        btnAction.setSize(100, 30);
        btnAction.setFocusPainted(false);
        btnAction.addActionListener(this);

        taLog = new JTextArea();
        taLog.setMargin(new Insets(5, 5, 5, 5));
        taLog.setSize(440, 200);
        taLog.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setLocation(20, 240);
        jScrollPane.setSize(440, 200);
        jScrollPane.setViewportView(taLog);

        add(tfInputSrcApk);
        add(tfDstSrcApk);
        add(hinSrcApkLabel);
        add(hinDstApkLabel);
        add(hinLogLabel);
        add(lbMemAndTimeUsed);
        add(btnSrcFileChooser);
        add(btnDstFileChooser);
        add(btnAction);
        add(jScrollPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnSrcFileChooser) {
            showFileChooserDialogAndSet(tfInputSrcApk);
        } else if (source == btnDstFileChooser) {
            showFileChooserDialogAndSet(tfDstSrcApk);
        } else if (source == btnAction) {
            String srcApkPath = tfInputSrcApk.getText();
            String dstApkPath = tfDstSrcApk.getText();
            if (mOnRequestAction != null) {
                mOnRequestAction.onRequest(srcApkPath, dstApkPath);
            }
        }
    }

    private void showFileChooserDialogAndSet(JTextField jTextField) {
        fcFileChooser.showOpenDialog(getContentPane());
        File file = fcFileChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        jTextField.setText(file.getAbsolutePath());
    }

    private String getMemAndTime(long memUsed, long time) {
        return String.format(Resources.RESOURCES.getString(ID_ACTION_SUMMARY), "" + memUsed, formatTime(time));
    }

    private String formatTime(long time) {
        if (time <= 0) {
            return "0.000 " + Resources.RESOURCES.getString(ID_SECONDS);
        }
        if (time > 60 * 60 * 1000) {
            return "more than one hour";
        }
        DecimalFormat decimalFormat = new DecimalFormat("00.000 " + Resources.RESOURCES.getString(ID_SECONDS));
        String seconds = decimalFormat.format(time % (1000 * 60) * 1.0 / 1000);
        String minutes = "";
        long temp = time / 1000 / 60 % 60;
        if (temp > 0) {
            minutes = temp + " " + Resources.RESOURCES.getString(ID_MINUTES) + " ";
        }
        return minutes + seconds;
    }

    public void setActionDescription(String description) {
        btnAction.setText(description);
    }

    public void setMemAndTimeUsed(long mem, long time) {
        lbMemAndTimeUsed.setText(getMemAndTime(mem, time));
    }

    public void updateApkPath(String srcApkPath, String dstApkPath) {
        tfInputSrcApk.setText(Util.isEmptyText(srcApkPath) ? "" : srcApkPath);
        tfDstSrcApk.setText(Util.isEmptyText(dstApkPath) ? "" : dstApkPath);
    }

    public void clearLog() {
        taLog.setText("");
    }

    public void appendLog(String logMsg) {
        taLog.append(logMsg + "\n");
    }

    public void setOnRequestAction(OnRequestAction action) {
        mOnRequestAction = action;
    }

    public interface OnRequestAction {
        void onRequest(String srcApkPath, String dstApkPath);
    }

}
