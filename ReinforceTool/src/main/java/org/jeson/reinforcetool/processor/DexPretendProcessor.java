package org.jeson.reinforcetool.processor;

import org.jeson.reinforce.shell.$$$.security.Encrypt;
import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.File;
import org.jeson.reinforcetool.strategy.StrategyManager;

import java.util.List;

/**
 * @Author zhangjisong on 2018/3/16.
 */

public class DexPretendProcessor implements ErrorCode {

    private Encrypt mEncrypt;

    public DexPretendProcessor() {
        this(Encrypt.DEFAULT);
    }

    public DexPretendProcessor(Encrypt encrypt) {
        mEncrypt = encrypt;
    }

    public synchronized void setEncrypt(Encrypt encrypt) {
        mEncrypt = encrypt;
    }

    public synchronized boolean process(File appWorkspaceDir, String dstMainClassName, List<DexFile> dexFilesToAppend) {
        return StrategyManager.DEX_PRETEND_STRATEGY.pretend(appWorkspaceDir, dstMainClassName, dexFilesToAppend, mEncrypt);
    }


}
