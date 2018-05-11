package org.jeson.reinforcetool.strategy;

import org.jeson.reinforce.shell.$$$.security.Encrypt;
import org.jeson.reinforcetool.file.DexFile;
import org.jeson.reinforcetool.file.File;

import java.util.List;

public interface IDexPretendStrategy {
    boolean pretend(File appWorkspaceDir, String dstMainClassName, List<DexFile> dexFilesToAppend, Encrypt encrypt);
}
