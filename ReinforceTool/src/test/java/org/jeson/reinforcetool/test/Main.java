package org.jeson.reinforcetool.test;

import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.processor.ApkProcessor;
import org.jeson.reinforcetool.processor.SignerProcessor;
import org.jeson.reinforcetool.processor.ZipalignProcessor;
import org.jeson.reinforcetool.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;

public class Main {

    @Before
    public void ready() {
        clean();
        File tempApk = new File("temp/temp.apk");
        assert tempApk.exists();
    }

//    @After
    public void clean() {
        File temp = new File("temp");
        if (!temp.exists()) {
            return;
        }
        FileUtil.delete(temp, "temp/temp.apk");
        File[] files = temp.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !"temp.apk".equals(name);
            }
        });
        assert files == null || files.length == 0;
    }

    @Test
    public void ApkDecodeAndEncodeTest() {
        ApkProcessor apkProcessor = new ApkProcessor("temp/test-workspace");
        assert apkProcessor.decodeApk("temp/temp.apk");
        assert apkProcessor.encodeApk("temp/temp-encoded.apk");
        apkProcessor.cleanWorkspace();
        System.out.println("encode and decode apk success!");
    }

    @Test
    public void zipAlignApkTest() {
        ZipalignProcessor zipalignProcessor = new ZipalignProcessor();
        int exitValue = zipalignProcessor.process("temp/temp.apk", null);
        System.out.println("exit value: " + exitValue);
        assert exitValue == ErrorCode.SUCCESS;
        System.out.println("zipalign apk success!");
    }

    @Test
    public void signApkTest() {
        SignerProcessor signerProcessor = new SignerProcessor();
        int exitValue = signerProcessor.sign("temp/temp.apk", null);
        System.out.println("exit value: " + exitValue);
        assert exitValue == ErrorCode.SUCCESS;
        System.out.println("sign apk success!");
    }

    @Test
    public void reinforceApkTest() {
        String[] args = new String[]{"-i", "temp/temp.apk"};
        int exitValue = new org.jeson.reinforcetool.Main.ReinforceProcess().process(args);
        System.out.println("exit value: " + exitValue);
        assert exitValue == ErrorCode.SUCCESS;
        System.out.println("reinforce apk success!");
    }

}
