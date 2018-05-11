package org.jeson.reinforce.util;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class FileUtil {

    private FileUtil() {
    }

    public static boolean closeIO(List<Closeable> closeable) {
        boolean isSuccess = true;
        for (Closeable c : closeable) {
            isSuccess &= closeIO(c);
        }
        return isSuccess;
    }

    public static boolean closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean loadPropertiesFromResources(String path, Properties properties) {
        if (Util.isEmptyText(path) || !path.endsWith(".properties")) {
            return false;
        }
        InputStream input = FileUtil.class.getResourceAsStream(path);
        if (input == null) {
            return false;
        }
        try {
            properties.load(new InputStreamReader(input, "utf-8"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(input);
        }
    }

    public static InputStream getInputStreamFromResources(String path) {
        if (Util.isEmptyText(path)) {
            return null;
        }
        return FileUtil.class.getResourceAsStream(path);
    }

    public static URL getURLFromResources(String path) {
        return FileUtil.class.getResource(path);
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

    public static boolean readAndWriteData(InputStream inputStream, OutputStream outputStream, boolean closeIn, boolean closeOut) {
        try {
            if (inputStream == null || outputStream == null) {
                return false;
            }
            byte[] buffer = new byte[1024];
            int len;
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
        InputStream in = null;
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
            return readAndWriteData(in, out, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(in);
            closeIO(out);
        }
    }

    public static void readLineInWorkThread(InputStream inputStream, ReadLineAction action) {
        if (inputStream == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (action != null) {
                            action.onReadLine(line);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeIO(reader);
                }
            }
        }).start();
    }

    public interface ReadLineAction {
        void onReadLine(String line);
    }

}
