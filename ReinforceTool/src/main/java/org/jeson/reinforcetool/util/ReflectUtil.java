package org.jeson.reinforcetool.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            Method method = clazz.getDeclaredMethod(name, classes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method.invoke(null, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fieldStatic(Class clazz, String name, Object value) {
        if (clazz == null) {
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object oldValue = field.get(null);
            field.set(null, value);
            return (T) oldValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
