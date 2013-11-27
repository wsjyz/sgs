package com.eighthinfo.sgs.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午9:24
 * To change this template use File | Settings | File Templates.
 */
public class ClassUtils {

    public void invokeMethod(String classMethodName,Object... params){
        String clazzName = classMethodName.substring(0,classMethodName.indexOf("."));
        String methodName = classMethodName.substring(classMethodName.indexOf("."),classMethodName.length());
        //invokeMethod(,methodName,String.class,params);

    }
    public static Type findClassType(String classType){
        Class clazz = null;
        try {
            clazz = Class.forName(classType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * 缓存方法
     */
    private static final Map<Class<?>, Method[]> METHODS_CACHEMAP = new HashMap<Class<?>, Method[]>();

    /**
     * 反射 取值、设值,合并两个对象(Field same only )
     *
     */
    public static <T> void copyProperties(T fromobj, T toobj, String... fieldspec) {
        for (String filename : fieldspec) {
            Object val = ClassUtils.invokeGetterMethod(fromobj, filename);
            ClassUtils.invokeSetterMethod(toobj, filename, val);
        }

    }

    public static void main(String[] args) {


    }

    /**调用Getter方法
     * @param obj			对象
     * @param propertyName  属性名
     */
    public static Object invokeGetterMethod(Object obj,String propertyName){
        String getterMethodName = "get"+StringUtils.capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, null, null);
    }

    /**调用Setter方法,不指定参数的类型
     * @param obj
     * @param propertyName
     * @param value
     */
    public static void invokeSetterMethod(Object obj,String propertyName,Object value){
        invokeSetterMethod(obj, propertyName, value, null);
    }

    /**调用Setter方法,指定参数的类型
     * @param obj
     * @param propertyName
     * @param value
     * @param propertyType 为空，则取value的Class
     */
    public static void invokeSetterMethod(Object obj,String propertyName,Object value,Class<?> propertyType){
        propertyType = propertyType != null ? propertyType : value
                .getClass();
        String setterMethodName = "set"+ StringUtils.capitalize(propertyName);
        invokeMethod(obj, setterMethodName, new Class<?>[]{propertyType}, new Object[]{value});
    }


    /**直接调用对象方法，忽视private/protected修饰符
     * @param obj
     * @param methodName
     * @param parameterTypes
     * @param args
     */
    public static Object invokeMethod(final Object obj,
                                      final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] args) {
        StopWatch clock = new StopWatch();
        clock.start();
        Method method = obtainAccessibleMethod(obj, methodName, parameterTypes);
        clock.stop();
        if (method == null) {
            throw new IllegalArgumentException(
                    "Devkit: Could not find method [" + methodName
                            + "] on target [" + obj + "].");
        }
        try {
            clock.reset();
            clock.start();
            Object object =  method.invoke(obj, args);
            clock.stop();
            return object;
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**循环向上转型，获取对象的DeclaredMethod,并强制设置为可访问
     * 如向上转型到Object仍无法找到，返回null
     *
     * 用于方法需要被多次调用的情况，先使用本函数先取得Method,然后调用Method.invoke(Object obj,Object... args)
     * @param obj
     * @param methodName
     * @param parameterTypes
     */
    public static Method obtainAccessibleMethod(final Object obj,
                                                final String methodName, final Class<?>... parameterTypes) {
        Class<?> superClass = obj.getClass();
        Class<Object> objClass = Object.class;
        for (; superClass != objClass; superClass = superClass.getSuperclass()) {
            Method method = null;
            try {
                method = superClass.getDeclaredMethod(methodName,
                        parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException | SecurityException e) {
                // Method不在当前类定义，继续向上转型
            }
        }
        return null;
    }

    /**
     * 不能确定方法是否包含参数时，通过方法名匹配获得方法
     * @param obj
     * @param methodName
     */
    public static Method obtainMethod(final Object obj,
                                      final String methodName){
        Class<?> clazz = obj.getClass();
        Method[] methods = METHODS_CACHEMAP.get(clazz);
        if (methods == null) { // 尚未缓存
            methods = clazz.getDeclaredMethods();
            METHODS_CACHEMAP.put(clazz, methods);
        }
        for (Method method : methods) {
            if (method.getName().equals(methodName))
                return method;
        }
        return null;

    }

    /**直接读取对象属性值
     * 忽视private/protected修饰符，不经过getter函数
     * @param obj
     * @param fieldName
     */
    public static Object obtainFieldValue(final Object obj,final String fieldName){
        Field field = obtainAccessibleField(obj, fieldName);
        if(field == null){
            throw new IllegalArgumentException("Devkit: could not find field ["+fieldName+"] on target ["+obj+"]");
        }
        Object retval = null;
        try {
            retval = field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return retval;

    }

    /**直接设置对象属性值
     * 忽视private/protected修饰符，不经过setter函数
     * @param obj
     * @param fieldName
     * @param value
     */
    public static void setFieldValue(final Object obj,final String fieldName,final Object value){
        Field field = obtainAccessibleField(obj, fieldName);
        if(field == null){
            throw new IllegalArgumentException("Devkit: could not find field ["+fieldName+"] on target ["+obj+"]");
        }
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**循环向上转型，获取对象的DeclaredField,并强制设为可访问
     * 如向上转型Object仍无法找到，返回null
     * @param obj
     * @param fieldName
     */
    public static Field obtainAccessibleField(final Object obj,
                                              final String fieldName) {
        Class<?> superClass = obj.getClass();
        Class<Object> objClass = Object.class;
        for (; superClass != objClass; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
