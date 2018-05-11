package org.jeson.reinforce.shell.$$$.processor;

import org.jeson.reinforce.shell.$$$.security.Encrypt;
import org.jeson.reinforce.shell.$$$.util.ApplicationUtil;
import org.jeson.reinforce.shell.$$$.util.Util;

import java.io.File;

public class ApplicationDefaultProcessor extends ApplicationProcessor {

    private static final String SOURCE_DEX_FILE_NAME = "assets/__source";

    protected boolean loadSourceDex(File apkFile, File tempFile) {
        return Util.loadFileFromZipFile(apkFile, tempFile, SOURCE_DEX_FILE_NAME, 0);
    }

    protected boolean splitDex(File sourceDexFile, File dexCacheDir) {
        return ApplicationUtil.splitDex(sourceDexFile, dexCacheDir, Encrypt.DEFAULT);
    }

}
