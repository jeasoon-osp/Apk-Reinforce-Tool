package org.jeson.reinforcetool.strategy;

import org.jeson.reinforce.shell.$$$.processor.ProcessorDefault;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.ReflectUtil;

import java.util.Properties;

public class StrategyManager {

    public static final IDexPretendStrategy DEX_PRETEND_STRATEGY;

    static {
        Properties properties = FileUtil.loadPropertiesFromResources("/apk/dex/dex.properties");
        DEX_PRETEND_STRATEGY = ReflectUtil.newInstance(properties.getProperty(ProcessorDefault.DEFAULT_PROCESSOR_NAME.substring(ProcessorDefault.DEFAULT_PROCESSOR_NAME.lastIndexOf(".") + 1)));
    }


}
