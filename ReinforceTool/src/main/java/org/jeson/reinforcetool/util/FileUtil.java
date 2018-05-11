package org.jeson.reinforcetool.util;

import org.jeson.reinforcetool.log.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class FileUtil {

    private FileUtil() {
    }

    public static boolean saveDocToFile(Document doc, File file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(file));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Properties loadPropertiesFromResources(String resourcePath) {
        Properties properties = new Properties();
        if (Util.isEmptyText(resourcePath)) {
            return properties;
        }
        InputStream in = null;
        try {
            in = FileUtil.class.getResourceAsStream(resourcePath);
            if (in == null) {
                return properties;
            }
            properties.load(in);
            return properties;
        } catch (Exception e) {
            Logger.DEFAULT.error(e.getMessage(), e);
            return properties;
        } finally {
            closeIO(in);
        }
    }

    public static boolean cloneDirFromResource(String resourcePath, String dstPath, boolean forceOverwrite) {
        if (Util.isEmptyText(resourcePath) || Util.isEmptyText(dstPath)) {
            return false;
        }
        URL url = FileUtil.class.getResource(resourcePath);
        if (url == null) {
            return false;
        }
        String urlFile = url.getFile();
        if (urlFile == null || !urlFile.matches("(jar:)?file:/.*!.*" + resourcePath)) {
            return false;
        }
        String   jarPathOrigin = urlFile.replaceAll(urlFile.matches("(jar:)?file:/[a-zA-Z]:") ? "(jar:)?file:/" : "(jar:)?file:", "");
        String[] pathPair      = jarPathOrigin.split("!/");
        if (pathPair.length < 2) {
            return false;
        }
        File jarFile = new File(pathPair[0]);
        if (!jarFile.exists()) {
            return false;
        }
        boolean      success    = true;
        String       dstDirPath = pathPair[1];
        File         dstDir     = new File(dstPath);
        InputStream  in         = null;
        OutputStream out        = null;
        JarFile      jar        = null;
        try {
            jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry next = entries.nextElement();
                if (!next.getName().startsWith(dstDirPath)) {
                    continue;
                }
                File newFile = new File(dstDir, next.getName());
                if (next.isDirectory()) {
                    if (!newFile.exists()) {
                        success &= newFile.mkdirs();
                    }
                    continue;
                }
                in = jar.getInputStream(next);
                if (newFile.exists()) {
                    if (in.available() == newFile.length() && next.getLastModifiedTime().to(TimeUnit.MILLISECONDS) == newFile.lastModified() && !forceOverwrite) {
                        closeIO(in);
                        in = null;
                        continue;
                    }
                    boolean ok = newFile.delete();
                    success &= ok;
                    if (!ok) {
                        closeIO(in);
                        in = null;
                        continue;
                    }
                }
                out = new FileOutputStream(newFile);
                success &= readAndWriteData(in, out, true, true);
                in = null;
                out = null;
            }
        } catch (IOException e) {
            Logger.DEFAULT.error(e.getMessage(), e);
        } finally {
            closeIO(in);
            closeIO(out);
            closeIO(jar);
        }
        return success;
    }

    public static boolean cloneFromResources(String resourcePath, String dstPath, boolean forceOverwrite) {
        if (Util.isEmptyText(resourcePath) || Util.isEmptyText(dstPath)) {
            return false;
        }
        File dstFile = new File(dstPath);
        if (dstFile.exists()) {
            if (dstFile.isDirectory()) {
                return false;
            }
        } else {
            File parentDir = dstFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    return false;
                }
            }
        }
        InputStream  in  = null;
        OutputStream out = null;
        try {
            in = FileUtil.class.getResourceAsStream(resourcePath);
            if (in == null) {
                return false;
            }
            if (dstFile.exists() && in.available() == dstFile.length() && !forceOverwrite) {
                return true;
            }
            out = new FileOutputStream(dstPath);
            return readAndWriteData(in, out);
        } catch (Exception e) {
            Logger.DEFAULT.error(e.getMessage(), e);
            return false;
        } finally {
            closeIO(in);
            closeIO(out);
        }
    }

    public static File getTempDir() {
        File file = getTempFile("");
        file.mkdirs();
        return file;
    }

    public static File getTempFile(String filePathInTempDir) {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (Util.isEmptyText(tempDirPath)) {
            tempDirPath = "";
        } else {
            if (!tempDirPath.endsWith(java.io.File.separator)) {
                tempDirPath = tempDirPath + java.io.File.separator;
            }
        }
        if (!Util.isEmptyText(filePathInTempDir)) {
            if (filePathInTempDir.startsWith("/") || filePathInTempDir.startsWith("\\")) {
                filePathInTempDir = filePathInTempDir.substring(1, filePathInTempDir.length());
            }
        }
        return new File(tempDirPath + "ApkReinforceTool" + File.separator + filePathInTempDir);
    }

    public static boolean readAndWriteData(InputStream inputStream, OutputStream outputStream) {
        return readAndWriteData(inputStream, outputStream, true, false);
    }

    public static boolean readAndWriteData(InputStream inputStream, OutputStream outputStream, boolean closeIn, boolean closeOut) {
        try {
            if (inputStream == null || outputStream == null) {
                return false;
            }
            byte[] buffer = new byte[1024];
            int    len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            return true;
        } catch (Exception e) {
        } finally {
            if (closeIn) {
                closeIO(inputStream);
            }
            if (closeOut) {
                closeIO(outputStream);
            }
        }
        return false;
    }

    public static boolean cloneFile(String srcPath, String dstPath) {
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if (!srcFile.exists()) {
            return false;
        }
        if (srcFile.isFile()) {
            FileInputStream  inputStream  = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(srcFile);
                outputStream = new FileOutputStream(dstFile);
                byte[] bytes = new byte[1024];
                int    len;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                    outputStream.flush();
                }
                return true;
            } catch (Exception e) {
                Logger.DEFAULT.error(e.getMessage(), e);
                return false;
            } finally {
                closeIO(inputStream);
                closeIO(outputStream);
            }
        } else if (srcFile.isDirectory()) {
            if (!dstFile.exists()) {
                boolean isSuccess = dstFile.mkdirs();
                if (!isSuccess) {
                    Logger.DEFAULT.error("copy dir failed, can't create dst dir: " + dstFile.getAbsolutePath());
                    return false;
                }
            }
            File[] files = srcFile.listFiles();
            if (files == null) {
                return false;
            }
            boolean isSuccess = true;
            for (File file : files) {
                if (file.isFile() || file.isDirectory()) {
                    isSuccess &= cloneFile(file.getAbsolutePath(), dstFile + File.separator + file.getName());
                }
            }
            return isSuccess;
        }
        return false;

    }

    public static boolean delete(String file, String... exclude) {
        return delete(new File(file), exclude);
    }

    public static boolean delete(org.jeson.reinforcetool.file.File file, String... exclude) {
        return delete(file.file(), exclude);
    }

    public static boolean delete(File file, String... exclude) {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            return true;
        }
        for (String excludePath : exclude) {
            if (file.getAbsolutePath().equals(new File(excludePath).getAbsolutePath())) {
                return true;
            }
        }
        if (file.isFile()) {
            return file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            boolean isSuccess = true;
            for (File f : files) {
                isSuccess &= delete(f, exclude);
            }
            isSuccess &= file.delete();
            return isSuccess;
        }
        return false;
    }

    public static boolean closeIO(List<Closeable> closeableList) {
        boolean isSuccess = true;
        for (Closeable closeable : closeableList) {
            isSuccess &= closeIO(closeable);
        }
        return isSuccess;
    }

    public static boolean closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    public static List<File> listFiles(File file) {
        return listFiles(file, false);
    }

    public static List<File> listFiles(File file, boolean includeSubDir) {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        if (file.isFile()) {
            return new ArrayList<>();
        }
        if (file.isDirectory()) {
            File[]     files      = file.listFiles();
            List<File> finalFiles = new ArrayList<>();
            for (File f : files) {
                finalFiles.add(f);
                if (f.isDirectory()) {
                    if (includeSubDir) {
                        finalFiles.addAll(listFiles(f, true));
                    }
                }
            }
            return finalFiles;
        }
        return new ArrayList<>();
    }

}
