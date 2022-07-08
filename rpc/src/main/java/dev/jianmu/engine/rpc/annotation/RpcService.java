package dev.jianmu.engine.rpc.annotation;

import java.lang.annotation.*;

/**
 * RPC 代理类注册
 * */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * 代理类名称
     * <br>
     * 当实现类实现多个接口时，请缺省 value 的值，程序将自动读取并配置所有接口
     * @apiNote 默认使用全限定名
     * */
    String value() default "";

}
