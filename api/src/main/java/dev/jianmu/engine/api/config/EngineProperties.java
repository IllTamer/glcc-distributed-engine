package dev.jianmu.engine.api.config;

import dev.jianmu.engine.rpc.provider.DefaultServiceProvider;
import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jianmu.engine")
public class EngineProperties {

    private Boolean deBug = false;

    @NotNull
    private CommonSerializer serializer = CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER);

    @NotNull
    private ServiceProvider serviceProvider = new DefaultServiceProvider();

}
