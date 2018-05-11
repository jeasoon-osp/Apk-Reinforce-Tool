package org.jeson.reinforce.resource;

import org.jeson.reinforce.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class Resources {
    private static final String LOCAL_NAME = Locale.getDefault().getLanguage();
    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String PATH_STRING_PROPERTIES = "/strings/strings";
    private static final String PATH_IMAGE = "/image/";

    public static final Resources RESOURCES = new Resources();

    private final Properties STRINGS_PROPERTIES = new Properties();

    private Resources() {
        loadStrings();
    }

    private void loadStrings() {
        FileUtil.loadPropertiesFromResources(PATH_STRING_PROPERTIES + PROPERTIES_SUFFIX, STRINGS_PROPERTIES);
        FileUtil.loadPropertiesFromResources(PATH_STRING_PROPERTIES + "-" + LOCAL_NAME + PROPERTIES_SUFFIX, STRINGS_PROPERTIES);
    }

    public String getString(String name) {
        return STRINGS_PROPERTIES.getProperty(name);
    }

    public InputStream open(String path) {
        return FileUtil.getInputStreamFromResources(path);
    }

    public ImageIcon getIcon(String name) {
        return new ImageIcon(FileUtil.getURLFromResources(PATH_IMAGE + name));
    }

    public Image getImage(String name) {
        return getIcon(name).getImage();
    }

}
