package tech.rfprojects.mybatisboost.spring.boot.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import tech.rfprojects.mybatisboost.limiter.LimiterInterceptor;
import tech.rfprojects.mybatisboost.mapper.MapperInterceptor;

@Configuration
@EnableConfigurationProperties(tech.rfprojects.mybatisboost.spring.boot.autoconfigure.MybatisBoostProperties.class)
@ConditionalOnBean({SqlSessionFactory.class, MapperScannerConfigurer.class})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisBoostAutoConfiguration {

    private final tech.rfprojects.mybatisboost.spring.boot.autoconfigure.MybatisBoostProperties properties;

    public MybatisBoostAutoConfiguration(tech.rfprojects.mybatisboost.spring.boot.autoconfigure.MybatisBoostProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public tech.rfprojects.mybatisboost.core.Configuration configuration() {
        return tech.rfprojects.mybatisboost.core.Configuration.builder()
                .setMultipleDatasource(properties.isMultipleDatasource()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperInterceptor mybatisInterceptor(tech.rfprojects.mybatisboost.core.Configuration configuration) {
        return new MapperInterceptor(configuration);
    }

    @Order(-1)
    @Bean
    @ConditionalOnMissingBean
    public LimiterInterceptor limiterInterceptor(tech.rfprojects.mybatisboost.core.Configuration configuration) {
        return new LimiterInterceptor(configuration);
    }
}
