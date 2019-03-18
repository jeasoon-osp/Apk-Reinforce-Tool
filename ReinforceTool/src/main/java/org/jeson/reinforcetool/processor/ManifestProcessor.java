package org.jeson.reinforcetool.processor;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultAttribute;
import org.jeson.reinforcetool.file.ManifestFile;
import org.jeson.reinforcetool.util.FileUtil;
import org.jeson.reinforcetool.util.Util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ManifestProcessor {

    private static final String ATTR_NAME = "name";

    private final String dstAppClassName;

    public ManifestProcessor() {
        this(null);
    }

    public ManifestProcessor(String dstAppClassName) {
        if (Util.isEmptyText(dstAppClassName)) {
            this.dstAppClassName = FileUtil.loadPropertiesFromResources("/apk/axml/axml.properties").getProperty("applicationName");
        } else {
            this.dstAppClassName = dstAppClassName;
        }
    }

    public String process(ManifestFile srcAxmlFile, ManifestFile dstAxmlFile) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(srcAxmlFile.file());
            Element root = document.getRootElement();
            Attribute attrCompileSdkVersion = root.attribute("compileSdkVersion");
            Attribute attrCompileSdkVersionCodename = root.attribute("compileSdkVersionCodename");
            if (attrCompileSdkVersion != null) {
                root.remove(attrCompileSdkVersion);
            }
            if (attrCompileSdkVersionCodename != null) {
                root.remove(attrCompileSdkVersionCodename);
            }
            Element application = root.element("application");
            Namespace androidNbsp = root.getNamespaceForPrefix("android");
            Attribute attrAppComponentFactory = application.attribute("appComponentFactory");
            if (attrAppComponentFactory != null) {
                application.remove(attrAppComponentFactory);
            }
            Attribute nameAttr = application.attribute("name");
            String applicationName = null;
            if (nameAttr != null) {
                String name = nameAttr.getValue();
                if (name.startsWith(".")) {
                    Attribute packageAttr = root.attribute("package");
                    name = packageAttr.getValue() + name;
                }
                applicationName = name;
                nameAttr.setValue(dstAppClassName);
            } else {
                nameAttr = new DefaultAttribute("name", dstAppClassName, androidNbsp);
                application.add(nameAttr);
            }
            XMLWriter writer = new XMLWriter(new PrintWriter(new OutputStreamWriter(dstAxmlFile.create())), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
            return applicationName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
