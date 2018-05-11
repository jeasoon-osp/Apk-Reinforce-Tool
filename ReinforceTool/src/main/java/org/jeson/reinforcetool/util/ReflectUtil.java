package org.jeson.reinforcetool.util;

public class ReflectUtil {

    private ReflectUtil() {
    }

    public static <T> T newInstance(String className) {
        try {
            return (T) Class.forName(className).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object invoke(Object obj, String name, Class[] classes, Object[] objects) {
        if (obj == null) {
            return null;
        }
        try {
            return obj.getClass().getDeclaredMethod(name, classes).invoke(obj, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeStatic(Class clazz, String name, Class[] classes, Object[] objects) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getDeclaredMethod(name, classes).invoke(null, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
