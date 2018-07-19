package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.MybatisInterceptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.core.mapper.MapperInterceptor;
import cn.mybatisboost.core.preprocessor.SingleParameterPreprocessor;
import cn.mybatisboost.lang.LangInterceptor;
import cn.mybatisboost.limiter.LimiterInterceptor;
import cn.mybatisboost.metric.MetricInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(MybatisBoostProperties.class)
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisBoostAutoConfiguration {

    private final MybatisBoostProperties properties;
    private final Environment environment;

    public MybatisBoostAutoConfiguration(MybatisBoostProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @Bean
    @ConditionalOnMissingBean
    public cn.mybatisboost.core.Configuration configuration()
            throws IllegalAccessException, InstantiationException {
        cn.mybatisboost.core.Configuration.Builder builder =
                cn.mybatisboost.core.Configuration.builder()
                        .setMultipleDatasource(properties.isMultipleDatasource())
                        .setShowQuery(properties.isShowQuery())
                        .setShowQueryWithParameters(properties.isShowQueryWithParameters())
                        .setSlowQueryThresholdInMillis(properties.getSlowQueryThresholdInMillis());
        if (properties.getNameAdaptor() != null) {
            builder.setNameAdaptor(properties.getNameAdaptor().newInstance());
        } else {
            builder.setNameAdaptor(new NoopNameAdaptor());
        }
        if (properties.getSlowQueryHandler() != null) {
            builder.setSlowQueryHandler(properties.getSlowQueryHandler().newInstance());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor mybatisInterceptor(cn.mybatisboost.core.Configuration configuration) {
        MybatisInterceptor interceptor = new MybatisInterceptor(configuration);
        interceptor.appendPreprocessor(new SingleParameterPreprocessor());
        if (matchConditionalProperty("mybatisboost.mapper.enabled")) {
            interceptor.appendInterceptor(new MapperInterceptor(configuration));
        }
        if (matchConditionalProperty("mybatisboost.lang.enabled")) {
            interceptor.appendInterceptor(new LangInterceptor(configuration));
        }
        if (matchConditionalProperty("mybatisboost.limiter.enabled")) {
            interceptor.appendInterceptor(new LimiterInterceptor(configuration));
        }
        if (matchConditionalProperty("mybatisboost.metric.enabled")) {
            interceptor.appendInterceptor(new MetricInterceptor(configuration));
        }
        return interceptor;
    }

    private boolean matchConditionalProperty(String name) {
        return environment.getProperty(name, boolean.class, false);
    }
}
