package org.jeson.reinforcetool.strategy.impl;

import org.jeson.reinforce.shell.$$$.security.Encrypt;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.File;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.zip.Adler32;

public class DexPretendMergeStrategy extends DexPretendDefaultStrategy {
    @Override
    public boolean pretend(File appWorkspaceDir, String dstMainClassName, List<DexFile> dexFilesToAppend, Encrypt encrypt) {
        String dstDexPath = appWorkspaceDir.path() + java.io.File.separator + "classes.dex";
        DexFile tmpFile = new DexFile(dstDexPath + "__.tmp");
        FileUtil.delete(tmpFile);
        boolean isSuccess = cloneShellDex(tmpFile);
        long dataOffset = tmpFile.size();
        isSuccess &= appendDex(tmpFile, dstMainClassName, dexFilesToAppend, encrypt);
        isSuccess &= appendDataOffset(tmpFile, dataOffset);
        byte[] bytes = tmpFile.bytes();
        isSuccess &= fixFileSizeHeader(bytes);
        isSuccess &= fixSha1Header(bytes);
        isSuccess &= fixCheckSumHeader(bytes);
        isSuccess &= clearDexAppended(dexFilesToAppend);
        FileUtil.write(bytes, tmpFile.create(false));
        FileUtil.delete(dstDexPath);
        isSuccess &= tmpFile.file().renameTo(new java.io.File(dstDexPath));
        return isSuccess;
    }

    protected boolean appendDataOffset(DexFile dexFile, long dataOffset) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(dexFile.create(true));
            out.writeLong(dataOffset);
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            FileUtil.closeIO(out);
        }
    }

    protected boolean fixFileSizeHeader(byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        byte[] sizeBytes = Util.reverse(Util.intToBytes(bytes.length));
        System.arraycopy(sizeBytes, 0, bytes, 32, 4);
        return true;
    }

    protected boolean fixSha1Header(byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(bytes, 32, bytes.length - 32);
            byte[] digestBytes = md.digest();
            System.arraycopy(digestBytes, 0, bytes, 12, 20);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean fixCheckSumHeader(byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        Adler32 adler32 = new Adler32();
        adler32.update(bytes, 12, bytes.length - 12);
        byte[] adler32Bytes = Util.reverse(Util.intToBytes((int) adler32.getValue()));
        System.arraycopy(adler32Bytes, 0, bytes, 8, 4);
        return true;
    }

}
