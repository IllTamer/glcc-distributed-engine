package dev.jianmu.engine.rpc.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtil {

    private ClassUtil() {}

    @Nullable
    public static Field getDeepField(String name, Class<?> clazz) {
        final List<Field> deepFields = getDeepFields(clazz);
        final List<Field> collect = deepFields.stream().filter(field -> field.getName().equals(name)).collect(Collectors.toList());
        return collect.size() > 0 ? collect.get(0) : null;
    }

    /**
     * 获取当前类及其父类所有成员变量
     * */
    @NotNull
    public static List<Field> getDeepFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null){
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 在已有类实例的前提下获取带注解的方法
     * @param obj 类实例
     * @param annotation 接口的类的Class对象
     * */
    public static HashMap<Method, Annotation> getMethods(Object obj, Class<? extends Annotation> annotation) {
        HashMap<Method, Annotation> hashMap = new HashMap<>(5);
        for (Method method : obj.getClass().getMethods())
            for (Annotation temp : method.getAnnotations())
                if (temp.annotationType() == annotation)
                    hashMap.put(method, temp);
        return hashMap;
    }

    public static HashMap<Class<?>, HashMap<Method, Annotation>> getMethods(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        HashMap<Class<?>, HashMap<Method, Annotation>> map = new HashMap<>(4);
        classes.forEach(c -> {
            HashMap<Method, Annotation> methods = new HashMap<>(3);
            for (Method method : c.getMethods()) {
                for (Annotation temp : method.getAnnotations()) {
                    if (temp.annotationType() == annotation) {
                        methods.put(method, temp);
                    }
                }
            }
            if (methods.size() != 0) {
                map.put(c, methods);
            }
        });
        return map;
    }
}
