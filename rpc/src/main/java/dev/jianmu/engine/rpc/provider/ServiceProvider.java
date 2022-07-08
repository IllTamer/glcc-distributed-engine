package dev.jianmu.engine.rpc.provider;

/**
 * 保存和提供服务实例对象
 * */
public interface ServiceProvider {

    /**
     * 保存服务到本地服务注册表
     * */
    <T> void addServiceProvider(T service, String serviceClass);

    /**
     * 从本地服务注册表获取服务
     * */
    Object getServiceProvider(String serviceName);

}
