package dev.jianmu.engine.rpc.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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

    public static Set<Class<?>> getClasses(File jarFile, ClassLoader classLoader) {
        if (!jarFile.exists()) {
            return new HashSet<>();
        }
        try {
            return gather(jarFile.toURI().toURL(), classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    private static Set<Class<?>> gather(URL jar, ClassLoader classLoader){
        Set<Class<?>> set = new HashSet<>();
        try (
                JarInputStream input = new JarInputStream(jar.openStream());
        ) {
            while (true) {
                JarEntry entry = input.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                String name = entry.getName();
                if (!name.isEmpty() && name.endsWith(".class")) {
                    System.out.println((name = name.substring(0, name.length()-6).replace('/', '.')));
                    set.add(classLoader.loadClass(name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }
}
