package me.rfprojects.spring.boot.autoconfigure;

import me.rfprojects.limiter.LimiterInterceptor;
import me.rfprojects.mapper.MapperInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@EnableConfigurationProperties({MybatisBoostProperties.class})
@AutoConfigureAfter({MybatisAutoConfiguration.class})
public class MybatisBoostAutoConfiguration {

    private final MybatisBoostProperties properties;

    public MybatisBoostAutoConfiguration(MybatisBoostProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public me.rfprojects.core.Configuration configuration() {
        return me.rfprojects.core.Configuration.builder()
                .setMultipleDatasource(properties.isMultipleDatasource()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperInterceptor mybatisInterceptor(me.rfprojects.core.Configuration configuration) {
        return new MapperInterceptor(configuration);
    }

    @Order(-1)
    @Bean
    @ConditionalOnMissingBean
    public LimiterInterceptor limiterInterceptor(me.rfprojects.core.Configuration configuration) {
        return new LimiterInterceptor(configuration);
    }
}
