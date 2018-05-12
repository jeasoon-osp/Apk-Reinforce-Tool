package org.jeson.reinforcetool.util;


/**
 * @Author zhangjisong on 2018/3/16.
 */

public class Util {
    public static final int OS_WIN   = 0;
    public static final int OS_MAC   = 1;
    public static final int OS_LINUX = 2;

    private Util() {
    }

    public static String cmd(String[] args) {
        String cmd = "";
        for (String arg : args) {
            cmd += arg + " ";
        }
        return cmd;
    }

    public static String formatTime(long time) {
        if (time <= 0) {
            return "0 Seconds 0 Milliseconds";
        }
        if (time > 60 * 60 * 1000) {
            return "more than one hour";
        }
        String minutes      = "";
        String seconds      = "";
        String milliseconds = "";
        long   temp;
        temp = time % 1000;
        if (temp > 0) {
            milliseconds = temp + " milliseconds ";
        }
        temp = time / 1000;
        temp %= 60;
        if (temp > 0) {
            seconds = temp + " seconds ";
        }
        temp = time / 1000 / 60;
        temp %= 60;
        if (temp > 0) {
            minutes = temp + " minutes ";
        }
        return minutes + seconds + milliseconds;
    }

    public static boolean isEmptyText(String text) {
        return text == null || text.isEmpty();
    }

    public static int getOsType() {
        String osName = System.getProperty("os.name");
        if (Util.isEmptyText(osName)) {
            return OS_WIN;
        }
        int osType;
        osName = osName.toLowerCase();
        if (osName.contains("win")) {
            osType = OS_WIN;
        } else if (osName.contains("mac")) {
            osType = OS_MAC;
        } else {
            osType = OS_LINUX;
        }
        return osType;
    }

    public static String getOsArch() {
        String arch = System.getProperty("java.vm.name");
        if (arch == null) {
            return "x86_linux";
        }
        if (arch.contains("64")) {
            arch = "x64";
        } else {
            arch = "x86";
        }
        String osName;
        int    os = getOsType();
        if (os == OS_WIN) {
            osName = "win";
        } else if (os == OS_MAC) {
            osName = "mac";
        } else {
            osName = "linux";
        }
        return arch + "_" + osName;
    }

    public static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (value >> (8 * (bytes.length - i - 1)));
        }
        return bytes;
    }

    public static byte[] reverse(byte[] bytes) {
        for (int i = 0; i < (bytes.length + 1) / 2; i++) {
            byte pre = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = pre;
        }
        return bytes;
    }

}
