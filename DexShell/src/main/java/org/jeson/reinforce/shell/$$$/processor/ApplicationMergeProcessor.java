package org.jeson.reinforce.shell.$$$.processor;

import org.jeson.reinforce.shell.$$$.util.Util;

import java.io.File;

public class ApplicationMergeProcessor extends ApplicationDefaultProcessor {

    private static final String SOURCE_DEX_FILE_NAME = "classes.dex";

    protected boolean loadSourceDex(File apkFile, File tempFile) {
        File tmpDexFile = new File(tempFile.getAbsolutePath() + "__dex");
        long offset     = Util.readLongFromZipFile(apkFile, tmpDexFile, SOURCE_DEX_FILE_NAME, -8);
        return Util.loadFileFromZipFile(apkFile, tempFile, SOURCE_DEX_FILE_NAME, offset);
    }
}
