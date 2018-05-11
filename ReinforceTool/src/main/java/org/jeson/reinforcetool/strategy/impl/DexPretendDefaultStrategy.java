package org.jeson.reinforcetool.strategy.impl;

import org.jeson.reinforce.shell.$$$.security.Encrypt;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.File;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.strategy.IDexPretendStrategy;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DexPretendDefaultStrategy implements IDexPretendStrategy {

    protected static final String NAME_DEX_PRETENDED;

    static {
        NAME_DEX_PRETENDED = FileUtil.loadPropertiesFromResources("/apk/dex/dex.properties").getProperty("dexPretenderName");
    }

    @Override
    public boolean pretend(File appWorkspaceDir, String dstMainClassName, List<DexFile> dexFilesToAppend, Encrypt encrypt) {
        String dstDexPath = appWorkspaceDir.path() + java.io.File.separator + "assets" + java.io.File.separator + NAME_DEX_PRETENDED;
        File tmpFile = new File(dstDexPath + "__.tmp");
        FileUtil.delete(tmpFile);
        boolean isSuccess = appendDex(tmpFile, dstMainClassName, dexFilesToAppend, encrypt);
        FileUtil.delete(dstDexPath);
        isSuccess &= tmpFile.file().renameTo(new java.io.File(dstDexPath));
        isSuccess &= clearDexAppended(dexFilesToAppend);
        isSuccess &= cloneShellDex(new DexFile(appWorkspaceDir.path() + java.io.File.separator + "classes.dex"));
        return isSuccess;
    }

    protected boolean appendDex(File dstFile, String dstMainClassName, List<DexFile> dexFilesToAppend, Encrypt encrypt) {
        List<Closeable> closeableList = new ArrayList<>();
        try {
            dstFile.mkParentDir();
            DataOutputStream out = new DataOutputStream(dstFile.create(true));
            closeableList.add(out);
            out.writeInt(0xCAFEBABE);
            if (Util.isEmptyText(dstMainClassName)) {
                out.writeInt(0);
            } else {
                byte[] buffer = dstMainClassName.getBytes("utf-8");
                out.writeInt(buffer.length);
                out.write(buffer);
                out.flush();
            }
            out.writeInt(dexFilesToAppend.size());
            boolean isSuccess = true;
            for (DexFile dexFile : dexFilesToAppend) {
                DexFile tempDexFile = new DexFile(dexFile.path() + "__encrypt.tmp");
                if (encrypt != null) {
                    isSuccess &= encrypt.encrypt(dexFile.file(), tempDexFile.file());
                    dexFile = tempDexFile;
                }
                out.writeLong(dexFile.size());
                InputStream in = dexFile.open();
                closeableList.add(in);
                isSuccess &= FileUtil.readAndWriteData(in, out);
                FileUtil.delete(tempDexFile);
            }
            FileUtil.closeIO(out);
            closeableList.remove(out);
            return isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            FileUtil.closeIO(closeableList);
        }

    }

    protected boolean clearDexAppended(List<DexFile> dexFilesToAppend) {
        if (dexFilesToAppend == null || dexFilesToAppend.size() == 0) {
            return true;
        }
        boolean isSuccess = true;
        for (DexFile dexFile : dexFilesToAppend) {
            isSuccess &= FileUtil.delete(dexFile);
        }
        return isSuccess;
    }

    protected boolean cloneShellDex(DexFile dexFile) {
        InputStream srcDexInputStream = getClass().getResourceAsStream("/apk/dex/classes.dex");
        if (srcDexInputStream == null) {
            Logger.DEFAULT.error("load shell dex file from resources dir failed!");
            return false;
        }
        OutputStream out = null;
        try {
            out = dexFile.create();
            return FileUtil.readAndWriteData(srcDexInputStream, out, true, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            FileUtil.closeIO(srcDexInputStream);
            FileUtil.closeIO(out);
        }
    }

}
