package org.jeson.reinforce.shell.$$$.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtil {

    public static final boolean DEBUGGABLE = true;

    private static final Map<String, Field> FIELDS_MAP = new HashMap<>();
    private static final Map<String, Method> METHOD_MAP = new HashMap<>();

    private ReflectUtil() {
    }

    public static <T> T newInstance(String className) {
        try {
            return (T) Class.forName(className).newInstance();
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object invokeStatic(String clazzName, String methodName) {
        try {
            return invokeStatic(Class.forName(clazzName), methodName, new Class[]{}, new Object[]{});
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object invokeStatic(String clazzName, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            return invokeStatic(Class.forName(clazzName), methodName, argTypes, args);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object invokeStatic(Class<?> clazz, String methodName) {
        return invokeStatic(clazz, methodName, new Class[]{}, new Object[]{});
    }

    public static Object invokeStatic(Class<?> clazz, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            String methodKey = clazz.getName() + "#" + methodName + ":" + Arrays.toString(argTypes);
            Method method = METHOD_MAP.get(methodKey);
            if (method == null) {
                method = clazz.getDeclaredMethod(methodName, argTypes);
                METHOD_MAP.put(methodKey, method);
            }
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object invoke(Object obj, String methodName) {
        return invoke(obj, methodName, new Class[]{}, new Object[]{});
    }

    public static Object invoke(Object obj, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, argTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            try {
                Method method = obj.getClass().getMethod(methodName, argTypes);
                method.setAccessible(true);
                return method.invoke(obj, args);
            } catch (Exception e1) {
                if (DEBUGGABLE) {
                    e.printStackTrace();
                    e1.printStackTrace();
                }
                return null;
            }
        }
    }

    public static Object fieldStatic(String clazzName, String fieldName) {
        try {
            return fieldStatic(Class.forName(clazzName), fieldName);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object fieldStatic(Class<?> clazz, String fieldName) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field = FIELDS_MAP.get(cacheKey);
            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (Exception ignored) {
                }
                FIELDS_MAP.put(cacheKey, field);
            }
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void fieldStatic(String clazzName, String fieldName, Object dst) {
        try {
            fieldStatic(Class.forName(clazzName), fieldName, dst);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
        }
    }

    public static void fieldStatic(Class<?> clazz, String fieldName, Object dst) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field = FIELDS_MAP.get(cacheKey);
            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (Exception ignored) {
                }
                FIELDS_MAP.put(cacheKey, field);
            }
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);
            field.set(null, dst);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
        }
    }

    public static Object field(Object obj, String fieldName) {
        try {
            return field(obj.getClass(), obj, fieldName);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object field(Class<?> clazz, Object obj, String fieldName) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field = FIELDS_MAP.get(cacheKey);
            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (Exception ignored) {
                }
                FIELDS_MAP.put(cacheKey, field);
            }
            if (field == null) {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null) {
                    return null;
                }
                return field(superClass, obj, fieldName);
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void field(Object obj, String fieldName, Object dst) {
        try {
            field(obj.getClass(), obj, fieldName, dst);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
        }
    }

    public static void field(Class<?> clazz, Object obj, String fieldName, Object dst) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field = FIELDS_MAP.get(cacheKey);
            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (Exception ignored) {
                }
                FIELDS_MAP.put(cacheKey, field);
            }
            if (field == null) {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null) {
                    throw new NoSuchFieldException(fieldName);
                }
                field(superClass, obj, fieldName, dst);
                return;
            }
            field.setAccessible(true);
            field.set(obj, dst);
        } catch (Exception e) {
            if (DEBUGGABLE) {
                e.printStackTrace();
            }
        }
    }

}
