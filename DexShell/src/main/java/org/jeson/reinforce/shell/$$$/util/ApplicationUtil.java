package org.jeson.reinforce.shell.$$$.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Process;
import dalvik.system.DexClassLoader;
import org.jeson.reinforce.shell.$$$.security.Encrypt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationUtil {

    private static final boolean DEBUGGABLE = true;

    private static final String ODEX_DIR_NAME = "__odex";
    private static final String CACHE_DEX_DIR_NAME = "__dex";
    private static final String VERSION_CODE_NAME = "__version";
    private static final String TMP_DEX_FILE_NAME = "__dex_source";
    private static final String APP_MAIN_CLASS_NAME = "__main_class";

    private static final String APP_APK;
    private static final String APP_LIB_DIR;
    private static final String APP_DATA_DIR;
    private static final String APP_CACHE_DIR;
    private static final String APP_PACKAGE_NAME;
    private static final Object APP_LOADED_APK;
    private static final Object APP_ACTIVITY_THREAD;
    private static final PackageInfo APP_PACKAGE_INFO;
    private static final ApplicationInfo APP_APPLICATION_INFO;

    static {
        APP_ACTIVITY_THREAD = getActivityThread();
        APP_LOADED_APK = getLoadedApk();
        APP_CACHE_DIR = System.getProperty("java.io.tmpdir");
        APP_APK = (String) ReflectUtil.field(APP_LOADED_APK, "mAppDir");
        APP_DATA_DIR = (String) ReflectUtil.field(APP_LOADED_APK, "mDataDir");
        APP_LIB_DIR = (String) ReflectUtil.field(APP_LOADED_APK, "mLibDir");
        APP_PACKAGE_NAME = (String) ReflectUtil.field(APP_LOADED_APK, "mPackageName");
        APP_PACKAGE_INFO = getPackageInfo();
        APP_APPLICATION_INFO = getApplicationInfo();
    }

    private ApplicationUtil() {
    }

    public static File checkDirAndMkDir(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static File getCacheDir() {
        return checkDirAndMkDir(new File(APP_CACHE_DIR));
    }

    public static File getCacheDir(String subDirName) {
        return checkDirAndMkDir(new File(getCacheDir(), subDirName));
    }

    public static File getCacheFile(String fileName) {
        return new File(getCacheDir(), fileName);
    }

    public static File getDataDir() {
        return checkDirAndMkDir(new File(APP_DATA_DIR));
    }

    public static File getDataDir(String subDirName) {
        return checkDirAndMkDir(new File(getDataDir(), subDirName));
    }

    public static File getDataFile(String fileName) {
        return new File(getDataDir(), fileName);
    }

    public static File getTmpDexFile() {
        return getCacheFile(TMP_DEX_FILE_NAME);
    }

    public static File getCacheDexDir() {
        return getDataDir(CACHE_DEX_DIR_NAME);
    }

    public static String getCacheDexPath(File dexDir) {
        File[] files = dexDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dex");
            }
        });
        if (files == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (File file : files) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(":");
            }
            sb.append(file.getAbsolutePath());
        }
        return sb.toString();
    }

    public static File getOdexDir() {
        return getDataDir(ODEX_DIR_NAME);
    }

    public static File getLibDir() {
        return new File(APP_LIB_DIR);
    }

    public static File getSrcApkFile() {
        return new File(APP_APK);
    }

    public static String getPackageName() {
        return APP_PACKAGE_NAME;
    }

    public static DexClassLoader newDexClassLoader(File dexDir, File odexDir, File libDir, ClassLoader parent) {
        return new DexClassLoader(getCacheDexPath(dexDir), odexDir.getAbsolutePath(), libDir.getAbsolutePath(), parent);
    }

    public static Object getLoadedApk() {
        try {
            if (APP_LOADED_APK != null) {
                return APP_LOADED_APK;
            }
            Object mBoundApplication = ReflectUtil.field(getActivityThread(), "mBoundApplication");
            return ReflectUtil.field(mBoundApplication, "info");
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object getActivityThread() {
        try {
            if (APP_ACTIVITY_THREAD != null) {
                return APP_ACTIVITY_THREAD;
            }
            return ReflectUtil.fieldStatic(Class.forName("android.app.ActivityThread"), "sCurrentActivityThread");
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo() {
        return (ApplicationInfo) ReflectUtil.field(getLoadedApk(), "mApplicationInfo");
    }

    public static PackageInfo getPackageInfo() {
        try {
            if (APP_PACKAGE_INFO != null) {
                return APP_PACKAGE_INFO;
            }
            Object pm = ReflectUtil.invokeStatic(getActivityThread().getClass(), "getPackageManager");
            Object pi = ReflectUtil.invoke(pm, "getPackageInfo", new Class[]{String.class, int.class, int.class}, new Object[]{APP_PACKAGE_NAME, 0, getMyUid()});
            if (pi == null) {
                pi = ReflectUtil.invoke(pm, "getPackageInfo", new Class[]{String.class, int.class}, new Object[]{APP_PACKAGE_NAME, 0});
            }
            return (PackageInfo) pi;
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static int getMyUid() {
        try {
            return (int) ReflectUtil.invokeStatic("android.os.UserHandle", "myUserId");
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return Process.myUid();
        }
    }

    public static ClassLoader appClassLoader(ClassLoader classLoader) {
        ClassLoader appClassLoader = null;
        try {
            Object loadedApk = getLoadedApk();
            appClassLoader = (ClassLoader) ReflectUtil.field(loadedApk, "mClassLoader");
            if (classLoader != null) {
                ReflectUtil.field(loadedApk, "mClassLoader", classLoader);
            }
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
        }
        return appClassLoader != null ? appClassLoader : ClassLoader.getSystemClassLoader();
    }

    public static ClassLoader getAppClassLoader() {
        return appClassLoader(null);
    }

    public static ClassLoader setAppClassLoader(ClassLoader classLoader) {
        return appClassLoader(classLoader);
    }

    public static int getVersionCode() {
        try {
            PackageInfo pi = getPackageInfo();
            if (pi == null) {
                ApplicationInfo ai = getApplicationInfo();
                return (int) ReflectUtil.field(ai, "versionCode");
            }
            return pi.versionCode;
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    public static File getLatestVersionFile() {
        return getDataFile(VERSION_CODE_NAME);
    }

    public static int getLatestVersionCode() {
        File versionCodeFile = getLatestVersionFile();
        if (!versionCodeFile.exists()) {
            return -1;
        }
        DataInputStream reader = null;
        try {
            reader = new DataInputStream(new FileInputStream(versionCodeFile));
            return reader.readInt();
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return -1;
        } finally {
            Util.closeIO(reader);
        }
    }

    public static int setLatestVersionCode(int version) {
        File versionCodeFile = getDataFile(VERSION_CODE_NAME);
        DataOutputStream writer = null;
        try {
            writer = new DataOutputStream(new FileOutputStream(versionCodeFile));
            writer.writeInt(version);
            writer.flush();
            return version;
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return -1;
        } finally {
            Util.closeIO(writer);
        }
    }

    public static String getMainAppClassName() {
        File classNameFile = new File(getCacheDexDir(), APP_MAIN_CLASS_NAME);
        if (!classNameFile.exists()) {
            return null;
        }
        return Util.readLineFromFile(classNameFile);
    }

    public static void setMainAppClassName(String className) {
        if (Util.isEmptyText(className)) {
            return;
        }
        File classNameFile = new File(getCacheDexDir(), APP_MAIN_CLASS_NAME);
        Util.recordLineToFile(classNameFile, className);
    }

    public static String loadMainAppClassNameFromFile(File source) {
        DataInputStream sourceIn = null;
        try {
            if (!source.exists() || !source.isFile()) {
                return null;
            }
            sourceIn = new DataInputStream(Util.open(source));
            if (0xCAFEBABE != sourceIn.readInt()) {
                return null;
            }
            int classNameSize = sourceIn.readInt();
            if (classNameSize == 0) {
                return null;
            }
            byte[] buffer = new byte[classNameSize];
            sourceIn.read(buffer);
            return new String(buffer, "utf-8");
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        } finally {
            Util.closeIO(sourceIn);
        }
    }

    public static boolean splitDex(File source, File dstDir, Encrypt encrypt) {
        List<Closeable> closeableList = new ArrayList<>();
        try {
            if (!source.exists() || !source.isFile()) {
                return false;
            }
            Util.delete(dstDir);
            dstDir.mkdirs();
            DataInputStream sourceIn = new DataInputStream(Util.open(source));
            closeableList.add(sourceIn);
            if (0xCAFEBABE != sourceIn.readInt()) {
                return false;
            }
            int classNameSize = sourceIn.readInt();
            if (classNameSize != 0) {
                sourceIn.skipBytes(classNameSize);
            }
            int count = sourceIn.readInt();
            byte[] buffer = new byte[1024];
            for (int i = 0; i < count; i++) {
                long size = sourceIn.readLong();
                int len;
                File dexFile = new File(dstDir + File.separator + "classes-" + i + ".dex");
                FileOutputStream outputStream = new FileOutputStream(dexFile);
                closeableList.add(outputStream);
                while (size > 0) {
                    int validSize = size > 1024 ? 1024 : (int) size;
                    len = sourceIn.read(buffer, 0, validSize);
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
                    size -= len;
                }
                Util.closeIO(outputStream);
                closeableList.remove(outputStream);
                if (encrypt != null) {
                    encrypt.decrypt(dexFile, dexFile);
                }
            }
            Util.closeIO(sourceIn);
            closeableList.remove(sourceIn);
            return true;
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return false;
        } finally {
            Util.closeIO(closeableList);
        }
    }

}
