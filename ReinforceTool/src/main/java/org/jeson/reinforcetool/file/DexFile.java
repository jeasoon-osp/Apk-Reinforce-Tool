package org.jeson.reinforcetool.file;


import org.jeson.reinforcetool.util.FileUtil;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author zhangjisong on 2018/3/16.
 */

public class DexFile extends File {

    public static final long MAGIC_NUMBER = 0x6465780A30333500L;
    public static final int CLASS_MAGIC_NUMBER = 0xCAFEBABE;

    public DexFile(String dexPath) {
        super(dexPath);
    }

    public DexFile(java.io.File file) {
        super(file);
    }

    public boolean checkMagicNumber() {
        RandomAccessFile raf = randomAccess();
        if (raf == null) {
            return false;
        }
        try {
            raf.seek(0);
            return raf.readLong() == MAGIC_NUMBER;
        } catch (IOException e) {
            return false;
        } finally {
            FileUtil.closeIO(raf);
        }
    }

    public boolean checkEOFMagicNumber() {
        RandomAccessFile raf = randomAccess();
        if (raf == null) {
            return true;
        }
        try {
            raf.seek(raf.length() - 5);
            return raf.readInt() != CLASS_MAGIC_NUMBER;
        } catch (IOException e) {
            return true;
        } finally {
            FileUtil.closeIO(raf);
        }

    }

}
