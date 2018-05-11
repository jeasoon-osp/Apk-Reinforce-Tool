package org.jeson.reinforce.shell.$$$.security;

import org.jeson.reinforce.shell.$$$.util.Util;

import java.io.File;

public interface Encrypt {

    Encrypt DEFAULT = new EncryptDefault();

    boolean encrypt(File srcFile, File dstFile);

    boolean decrypt(File srcFile, File dstFile);

    class EncryptDefault implements Encrypt {

        @Override
        public boolean encrypt(File srcFile, File dstFile) {
            if (srcFile.getAbsolutePath().equals(dstFile.getAbsolutePath())) {
                return true;
            }
            Util.delete(dstFile);
            return srcFile.renameTo(dstFile);
        }

        @Override
        public boolean decrypt(File srcFile, File dstFile) {
            if (srcFile.getAbsolutePath().equals(dstFile.getAbsolutePath())) {
                return true;
            }
            Util.delete(dstFile);
            return srcFile.renameTo(dstFile);
        }
    }
}
