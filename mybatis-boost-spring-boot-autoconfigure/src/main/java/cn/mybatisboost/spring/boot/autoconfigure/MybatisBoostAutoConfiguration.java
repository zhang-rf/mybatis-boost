package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.MybatisInterceptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.core.preprocessor.ParameterMappingsPreprocessor;
import cn.mybatisboost.core.preprocessor.ParameterNormalizationPreprocessor;
import cn.mybatisboost.lang.LangInterceptor;
import cn.mybatisboost.limiter.LimiterInterceptor;
import cn.mybatisboost.mapper.MapperInterceptor;
import cn.mybatisboost.metric.MetricInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MybatisBoostProperties.class)
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisBoostAutoConfiguration {

    private final MybatisBoostProperties properties;

    @Value("${mybatisboost.mapper.enabled:true}")
    private boolean isMapperEnabled;
    @Value("${mybatisboost.lang.enabled:true}")
    private boolean isLangEnabled;
    @Value("${mybatisboost.limiter.enabled:true}")
    private boolean isLimiterEnabled;
    @Value("${mybatisboost.metric.enabled:true}")
    private boolean isMetricEnabled;

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
    public MetricInterceptor metricInterceptor(cn.mybatisboost.core.Configuration configuration) {
        return isMetricEnabled ? new MetricInterceptor(configuration) : null;
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor mybatisInterceptor(cn.mybatisboost.core.Configuration configuration) {
        MybatisInterceptor mybatisInterceptor = new MybatisInterceptor(configuration);
        mybatisInterceptor.appendPreprocessor(new ParameterMappingsPreprocessor());
        mybatisInterceptor.appendPreprocessor(new ParameterNormalizationPreprocessor());
        if (isMapperEnabled) {
            mybatisInterceptor.appendInterceptor(new MapperInterceptor(configuration));
        }
        if (isLangEnabled) {
            mybatisInterceptor.appendInterceptor(new LangInterceptor(configuration));
        }
        if (isLimiterEnabled) {
            mybatisInterceptor.appendInterceptor(new LimiterInterceptor(configuration));
        }
        return mybatisInterceptor;
    }
}
