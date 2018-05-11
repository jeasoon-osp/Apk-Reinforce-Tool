package org.jeson.reinforce.shell.$$$.processor;

import org.jeson.reinforce.shell.$$$.util.Util;

import java.io.File;

public class ApplicationMergeProcessor extends ApplicationDefaultProcessor {

    private static final String SOURCE_DEX_FILE_NAME = "classes.dex";

    protected boolean loadSourceDex(File apkFile, File tempFile) {
        byte[] bytes = Util.readDataFromZipFile(apkFile, SOURCE_DEX_FILE_NAME, -8, 8);
        long offset = Util.bytesToLong(bytes);
        return Util.loadFileFromZipFile(apkFile, tempFile, SOURCE_DEX_FILE_NAME, offset);
    }
}
