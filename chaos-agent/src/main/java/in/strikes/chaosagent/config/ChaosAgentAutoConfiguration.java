package in.strikes.chaosagent.config;

import in.strikes.chaosagent.core.ChaosFaultRegistry;
import in.strikes.chaosagent.core.ChaosInjectionAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ChaosAgentProperties.class)
@ConditionalOnProperty(prefix = "chaos.agent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChaosAgentAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public ChaosFaultRegistry chaosFaultRegistry(ChaosAgentProperties properties) {
        return new ChaosFaultRegistry(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChaosInjectionAspect chaosInjectionAspect(ChaosFaultRegistry registry) {
        return new ChaosInjectionAspect(registry);
    }
}
