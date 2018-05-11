package org.jeson.reinforce.shell.$$$.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Util {

    private Util() {
    }

    public static OutputStream create(File file) {
        return create(file, false);
    }

    public static OutputStream create(File file, boolean append) {
        try {
            return new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream open(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean readAndWriteData(InputStream inputStream, OutputStream outputStream) {
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
            e.printStackTrace();
            return false;
        } finally {
            closeIO(inputStream);
        }
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

    public static boolean closeIO(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    public static boolean closeIO(List<Closeable> closeable) {
        if (closeable == null) {
            return false;
        }
        boolean isSuccess = true;
        for (Closeable clo : closeable) {
            isSuccess &= closeIO(clo);
        }
        return isSuccess;
    }

    public static boolean isEmptyDir(File dir) {
        if (!dir.exists()) {
            return true;
        }
        if (dir.isFile()) {
            return true;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dex");
            }
        });
        return files == null || files.length == 0;
    }

    public static String readLineFromFile(File file) {
        BufferedReader reader = null;
        try {
            if (!file.exists()) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(reader);
        }
    }

    public static void recordLineToFile(File file, String line) {
        PrintWriter printWriter = null;
        try {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            printWriter.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(printWriter);
        }
    }

    public static boolean delete(File file) {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            return false;
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
                isSuccess &= delete(f);
            }
            isSuccess &= file.delete();
            return isSuccess;
        }
        return false;
    }

    public static long readLongFromZipFile(File srcFile, File tempFile, String fileName, long offset) {
        List<Closeable> closeableList = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            closeableList.add(zipFile);
            ZipEntry fileEntry = zipFile.getEntry(fileName);
            if (fileEntry == null) {
                return -1;
            }
            FileOutputStream tmpOut = new FileOutputStream(tempFile);
            closeableList.add(tmpOut);
            readAndWriteData(zipFile.getInputStream(fileEntry), tmpOut);
            closeIO(tmpOut);
            closeableList.remove(zipFile);
            closeableList.remove(tmpOut);
            DataInputStream inputStream = new DataInputStream(new FileInputStream(tempFile));
            closeableList.add(inputStream);
            if (offset < 0) {
                offset = tempFile.length() + offset;
            }
            inputStream.skip(offset);
            long result = inputStream.readLong();
            Util.closeIO(inputStream);
            Util.closeIO(zipFile);
            closeableList.remove(inputStream);
            tempFile.delete();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            Util.closeIO(closeableList);
        }
    }

    public static boolean loadFileFromZipFile(File srcFile, File dstFile, String fileName, long skipCount) {
        List<Closeable> closeableList = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            closeableList.add(zipFile);
            ZipEntry fileEntry = zipFile.getEntry(fileName);
            if (fileEntry == null) {
                return false;
            }
            InputStream entryIn = zipFile.getInputStream(fileEntry);
            closeableList.add(entryIn);
            entryIn.skip(skipCount);
            FileOutputStream fileOut = new FileOutputStream(dstFile);
            closeableList.add(fileOut);
            boolean isSuccess = Util.readAndWriteData(entryIn, fileOut);
            Util.closeIO(entryIn);
            closeableList.remove(entryIn);
            Util.closeIO(fileOut);
            closeableList.remove(fileOut);
            Util.closeIO(zipFile);
            closeableList.remove(zipFile);
            return isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Util.closeIO(closeableList);
        }
    }

    public static String loadDirFromZipFile(File srcFile, File dstDir, String dirName) {
        List<Closeable> closeableList = new ArrayList<>();
        try {
            if (!dirName.endsWith("/")) {
                dirName = dirName + "/";
            }
            File                            libDir  = dstDir;
            ZipFile                         zipFile = new ZipFile(srcFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry     = entries.nextElement();
                String   entryName = entry.getName();
                System.err.println("dstDir = " + dstDir.getAbsolutePath());
                System.err.println("entryName = " + entryName);
                if (!entryName.startsWith(dirName)) {
                    continue;
                }
                InputStream entryIn = zipFile.getInputStream(entry);
                closeableList.add(entryIn);
                File lib        = new File(dstDir, entryName.substring(dirName.length()));
                File parentFile = lib.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                libDir = parentFile;
                FileOutputStream fileOut = new FileOutputStream(lib);
                closeableList.add(fileOut);
                Util.readAndWriteData(entryIn, fileOut);
                Util.closeIO(fileOut);
                closeableList.remove(fileOut);
                Util.closeIO(entryIn);
                closeableList.remove(entryIn);
            }
            return libDir.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Util.closeIO(closeableList);
        }
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static boolean isEmptyText(String text) {
        return text == null || text.isEmpty();
    }


    public static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= bytes[i] << (8 * (bytes.length - i - 1));
        }
        return value;
    }
}
