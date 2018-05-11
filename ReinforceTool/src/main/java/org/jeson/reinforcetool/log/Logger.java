package org.jeson.reinforcetool.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author zhangjisong on 2018/3/16.
 */

public class Logger {

    public static boolean DEBUG = true;

    public static final Logger DEFAULT = getLogger(Logger.class.getSimpleName());

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS", Locale.getDefault());

    private final String mTag;

    private LogInterceptor mLogInterceptor;

    private Logger(String tag) {
        mTag = tag;
    }

    public void info(String msg) {
        if (!DEBUG) {
            return;
        }
        if (msg == null) {
            msg = "";
        }
        msg = String.format("%s INFO %s: %s", mDateFormat.format(new Date()), mTag, msg);
        dispatchMsg(msg, null);
    }

    public void error(String msg) {
        error(msg, null);
    }

    public void error(String msg, Throwable t) {
        if (!DEBUG) {
            return;
        }
        if (msg == null) {
            msg = "";
        }
        msg = String.format("%s ERROR %s: %s", mDateFormat.format(new Date()), mTag, msg);
        dispatchMsg(msg, t);
    }

    private void dispatchMsg(String msg, Throwable t) {
        if (mLogInterceptor != null && mLogInterceptor.onInterceptor(msg, t)) {
            return;
        }
        System.out.println(msg);
        if (t != null) {
            t.printStackTrace();
        }
    }

    public static Logger getLogger(String tag) {
        return new Logger(tag);
    }

    public void setLogInterceptor(LogInterceptor interceptor) {
        mLogInterceptor = interceptor;
    }

    public interface LogInterceptor {
        boolean onInterceptor(String msg, Throwable t);
    }

}
