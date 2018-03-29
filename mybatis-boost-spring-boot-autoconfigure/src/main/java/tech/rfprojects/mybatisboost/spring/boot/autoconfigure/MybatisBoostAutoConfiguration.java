package tech.rfprojects.mybatisboost.spring.boot.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import tech.rfprojects.mybatisboost.core.adaptor.NoopNameAdaptor;
import tech.rfprojects.mybatisboost.limiter.LimiterInterceptor;
import tech.rfprojects.mybatisboost.mapper.MapperInterceptor;

@Configuration
@EnableConfigurationProperties(MybatisBoostProperties.class)
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisBoostAutoConfiguration {

    private final MybatisBoostProperties properties;

    public MybatisBoostAutoConfiguration(MybatisBoostProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public tech.rfprojects.mybatisboost.core.Configuration configuration()
            throws IllegalAccessException, InstantiationException {
        tech.rfprojects.mybatisboost.core.Configuration.Builder builder = tech.rfprojects.mybatisboost.core.Configuration.builder()
                .setMultipleDatasource(properties.isMultipleDatasource());
        if (properties.getNameAdaptor() != null) {
            builder.setNameAdaptor(properties.getNameAdaptor().newInstance());
        } else {
            builder.setNameAdaptor(new NoopNameAdaptor());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.limiter.enabled", havingValue = "true", matchIfMissing = true)
    @Order(-1)
    public LimiterInterceptor limiterInterceptor(tech.rfprojects.mybatisboost.core.Configuration configuration) {
        return new LimiterInterceptor(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.mapper.enabled", havingValue = "true", matchIfMissing = true)
    public MapperInterceptor mybatisInterceptor(tech.rfprojects.mybatisboost.core.Configuration configuration) {
        return new MapperInterceptor(configuration);
    }
}
