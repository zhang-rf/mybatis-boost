package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.core.mapper.MapperInterceptor;
import cn.mybatisboost.lang.LangInterceptor;
import cn.mybatisboost.limiter.LimiterInterceptor;
import cn.mybatisboost.metric.MetricInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public cn.mybatisboost.core.Configuration configuration()
            throws IllegalAccessException, InstantiationException {
        cn.mybatisboost.core.Configuration.Builder builder =
                cn.mybatisboost.core.Configuration.builder()
                        .setMultipleDatasource(properties.isMultipleDatasource())
                        .setLogSqlAndTime(properties.isLogSqlAndTime())
                        .setSlowSqlThresholdInMillis(properties.getSlowSqlThresholdInMillis());
        if (properties.getNameAdaptor() != null) {
            builder.setNameAdaptor(properties.getNameAdaptor().newInstance());
        } else {
            builder.setNameAdaptor(new NoopNameAdaptor());
        }
        if (properties.getSlowSqlHandler() != null) {
            builder.setSlowSqlHandler(properties.getSlowSqlHandler().newInstance());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.metric.enabled", havingValue = "true", matchIfMissing = true)
    public MetricInterceptor metricInterceptor(cn.mybatisboost.core.Configuration configuration) {
        return new MetricInterceptor(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.limiter.enabled", havingValue = "true", matchIfMissing = true)
    public LimiterInterceptor limiterInterceptor(cn.mybatisboost.core.Configuration configuration) {
        return new LimiterInterceptor(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.lang.enabled", havingValue = "true", matchIfMissing = true)
    public LangInterceptor langInterceptor(cn.mybatisboost.core.Configuration configuration) {
        return new LangInterceptor(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "mybatisboost.mapper.enabled", havingValue = "true", matchIfMissing = true)
    public MapperInterceptor mapperInterceptor(cn.mybatisboost.core.Configuration configuration) {
        return new MapperInterceptor(configuration);
    }
}
