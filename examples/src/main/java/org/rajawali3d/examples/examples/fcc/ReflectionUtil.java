package org.rajawali3d.examples.examples.fcc;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {

    // 反射调用无参方法
    public static Object invokeMethod(Object obj, String methodName) {
        if (obj != null && !TextUtils.isEmpty(methodName)) {
            Class clazz = obj.getClass();
            try {
                Method method = clazz.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(obj);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("fcc", "invokeMethod: "+e);
            }
        }

        return null;
    }

    public static Object invokeMethod(Object obj, String methodName, Object[] params) {
        if (obj != null && !TextUtils.isEmpty(methodName)) {
            Class clazz = obj.getClass();

            try {
                Class<?>[] paramTypes = null;
                if (params != null) {
                    paramTypes = new Class[params.length];

                    for(int i = 0; i < params.length; ++i) {
                        paramTypes[i] = params[i].getClass();
                    }
                }

                Method method = clazz.getMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method.invoke(obj, params);
            } catch (NoSuchMethodException var6) {
            } catch (Exception var7) {
                var7.printStackTrace();
            }

            return null;
        } else {
            return null;
        }
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj != null && !TextUtils.isEmpty(fieldName)) {
            Class clazz = obj.getClass();

            while(clazz != Object.class) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (Exception var4) {
                    clazz = clazz.getSuperclass();
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj != null && !TextUtils.isEmpty(fieldName)) {
            Class clazz = obj.getClass();

            while(clazz != Object.class) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, value);
                    return;
                } catch (Exception var5) {
                    clazz = clazz.getSuperclass();
                }
            }

        }
    }
}
