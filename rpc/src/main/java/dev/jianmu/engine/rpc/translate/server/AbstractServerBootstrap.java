package dev.jianmu.engine.rpc.translate.server;

import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.service.ServiceRegistry;
import dev.jianmu.engine.rpc.util.Assert;
import dev.jianmu.engine.rpc.util.ClassUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractServerBootstrap {

    protected final String host;
    protected final Integer port;
    protected final ServiceRegistry serviceRegistry;
    protected final ServiceProvider serviceProvider;
    private final Function<Class<?>, Object> function;

    public AbstractServerBootstrap(
            @NotNull String host,
            @NotNull Integer port,
            @NotNull ServiceRegistry serviceRegistry,
            @NotNull ServiceProvider serviceProvider,
            @NotNull String servicePackage,
            @Nullable Map<String, Class<?>> serviceMap,
            @NotNull Function<Class<?>, Object> function
    ) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
        this.serviceProvider = serviceProvider;
        this.function = function;
        scanServices(servicePackage, serviceMap == null ? new TreeMap<>() : serviceMap);
    }

    /**
     * 注册RPC服务实例
     * <br>
     * 优先级: spring bean > config serviceMap > scan Classes
     * @param servicePackage 服务器注解 @ServiceScan 的扫描路径
     * */
    @SneakyThrows(UnsupportedEncodingException.class)
    private void scanServices(@NotNull String servicePackage, @NotNull Map<String, Class<?>> serviceMap) {
        Assert.notNull(servicePackage, "The 'servicePackage' can not be null");
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        File file = new File(URLDecoder.decode(path, "UTF-8"));
        Assert.isTrue(file.exists(), "Can not find jar file");

        Collection<Class<?>> configServices = serviceMap.values();
        Set<Class<?>> classSet = ClassUtil.getClasses(servicePackage, this.getClass().getClassLoader(), false).stream()
                .filter(clazz -> clazz.isAnnotationPresent(RpcService.class) && !configServices.contains(clazz))
                .collect(Collectors.toSet());
        for (Map.Entry<String, Class<?>> entry : serviceMap.entrySet()) {
            Object instance = function.apply(entry.getValue());
            if (instance != null) {
                publishService(instance, entry.getKey());
                continue;
            }
            createAndPublishService(entry.getValue(), entry.getKey());
        }

        for (Class<?> clazz : classSet) {
            String serviceName = clazz.getAnnotation(RpcService.class).value();
            createAndPublishService(clazz, serviceName);
        }
    }

    private void createAndPublishService(Class<?> clazz, String serviceName) {
        Object instance;
        try {
            instance = clazz.newInstance();
            if (serviceName.length() == 0) {
                // 一个服务 Impl 类可能实现类多个服务接口
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> _interface : interfaces) {
                    publishService(instance, _interface.getCanonicalName());
                }
            } else {
                publishService(instance, serviceName);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            log.warn("创建 {} 时出错", clazz, e);
        }
    }

    /**
     * 发布 {@link RpcService}
     * */
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}
