package org.jeson.reinforcetool.file;

import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.util.FileUtil;

import java.io.*;

public class File {

    private final java.io.File file;

    public File(String dexPath) {
        file = new java.io.File(dexPath);
    }

    public File(java.io.File file) {
        this.file = file;
    }

    public boolean checkMagicNumber() {
        return true;
    }

    public boolean exists() {
        return file.exists();
    }

    public java.io.File file() {
        return file;
    }

    public boolean mkParentDir() {
        java.io.File parentDir = file.getParentFile();
        return parentDir.exists() || parentDir.mkdirs();
    }

    public String name() {
        return file.getName();
    }

    public String path() {
        return file.getAbsolutePath();
    }

    public long size() {
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return -1L;
    }

    @Override
    public String toString() {
        return path();
    }

    public byte[] bytes() {
        InputStream in = null;
        try {
            in = open();
            byte[] bytes = new byte[(int) size()];
            if (in.read(bytes) == size()) {
                return bytes;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            FileUtil.closeIO(in);
        }
    }

    public RandomAccessFile randomAccess() {
        if (!file.exists()) {
            Logger.DEFAULT.error("dex file does not exist！");
            return null;
        }
        if (file.isDirectory()) {
            Logger.DEFAULT.error("dex file is a directory！");
            return null;
        }
        try {
            return new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            Logger.DEFAULT.error("dex file open failed！", e);
            return null;
        }
    }

    public InputStream open() {
        if (!file.exists()) {
            Logger.DEFAULT.error("dex file does not exist！");
            return null;
        }
        if (file.isDirectory()) {
            Logger.DEFAULT.error("dex file is a directory！");
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.DEFAULT.error("dex file open failed！", e);
            return null;
        }
    }

    public OutputStream create() {
        return create(false);
    }

    public OutputStream create(boolean append) {
        if (file.isDirectory()) {
            Logger.DEFAULT.error("dex file is a directory！");
            return null;
        }
        try {
            java.io.File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    return null;
                }
            }
            return new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            Logger.DEFAULT.error("dex file create failed！", e);
            return null;
        }
    }

}
