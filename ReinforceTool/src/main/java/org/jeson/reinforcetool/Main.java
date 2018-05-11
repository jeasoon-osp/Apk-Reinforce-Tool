package org.jeson.reinforcetool;

import org.jeson.commons.cli.*;
import org.jeson.reinforcetool.error.ErrorCode;
import org.jeson.reinforcetool.log.Logger;
import org.jeson.reinforcetool.processor.ReinforceProcessor;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(new ReinforceProcess().process(args));
    }

    public static class ReinforceProcess implements ErrorCode {

        private static final String INPUT_PATH = "i";
        private static final String OUTPUT_PATH = "o";
        private static final String SIGNER_PROPERTIES_PATH = "s";

        private final Options OPTIONS = new Options();
        private final HelpFormatter HELP_FORMATTER = new HelpFormatter();
        private final CommandLineParser COMMAND_LINE_PARSER = new DefaultParser();

        public ReinforceProcess() {
            OPTIONS.addRequiredOption(INPUT_PATH, "input", true, "input apk path");
            OPTIONS.addOption(OUTPUT_PATH, "output", true, "output apk path");
            OPTIONS.addOption(SIGNER_PROPERTIES_PATH, "sign", true, "signer properties path");
        }

        private synchronized CommandLine parseArgs(String[] args) {
            try {
                return COMMAND_LINE_PARSER.parse(OPTIONS, args, true);
            } catch (ParseException e) {
                return null;
            }
        }

        private Properties parseProperties(String propertiesOri) {
            if (Util.isEmptyText(propertiesOri)) {
                return null;
            }
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(propertiesOri);
                properties.load(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtil.closeIO(inputStream);
            }
            return properties;
        }

        public int process(String[] args) {
            if (args == null) {
                printUsage();
                return FAILED_ARGS_ERROR;
            }
            CommandLine cmdLine = parseArgs(args);
            if (cmdLine == null) {
                printUsage();
                return FAILED_ARGS_ERROR;
            }
            if (!cmdLine.hasOption(INPUT_PATH)) {
                printUsage();
                return FAILED_ARGS_ERROR;
            }
            Properties signerProperties = null;
            if (cmdLine.hasOption(SIGNER_PROPERTIES_PATH)) {
                signerProperties = parseProperties(cmdLine.getOptionValue(SIGNER_PROPERTIES_PATH));
            }
            String inputPath = cmdLine.getOptionValue(INPUT_PATH);
            String outputPath = cmdLine.getOptionValue(OUTPUT_PATH);
            long startTime = System.currentTimeMillis();
            int exitValue = new ReinforceProcessor(inputPath, outputPath).setSignerProperties(signerProperties).process();
            long endTime = System.currentTimeMillis();
            System.out.println();
            Logger.DEFAULT.info("Reinforce " + (exitValue == SUCCESS ? "success!" : "failed!") + " Memory used: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB" + ", Time used: " + Util.formatTime(endTime - startTime));
            return exitValue;
        }

        private void printUsage() {
            HELP_FORMATTER.printHelp(" ", OPTIONS, true);
        }


    }

}
