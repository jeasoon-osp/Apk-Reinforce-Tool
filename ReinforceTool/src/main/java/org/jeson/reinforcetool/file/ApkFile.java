package org.jeson.reinforcetool.file;

import org.jeson.reinforcetool.util.FileUtil;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author zhangjisong on 2018/3/16.
 */

public class ApkFile extends File {

    public static final long MAGIC_NUMBER = 0x504B0304L;

    public ApkFile(String dexPath) {
        super(dexPath);
    }

    public ApkFile(java.io.File file) {
        super(file);
    }

    @Override
    public boolean checkMagicNumber() {
        RandomAccessFile raf = randomAccess();
        if (raf == null) {
            return false;
        }
        try {
            raf.seek(0);
            return raf.readInt() == MAGIC_NUMBER;
        } catch (IOException e) {
            return false;
        } finally {
            FileUtil.closeIO(raf);
        }
    }
}
