package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.MybatisBoostInterceptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.core.preprocessor.ParameterMappingsPreprocessor;
import cn.mybatisboost.core.preprocessor.ParameterNormalizationPreprocessor;
import cn.mybatisboost.lang.LangInterceptor;
import cn.mybatisboost.limiter.LimiterInterceptor;
import cn.mybatisboost.mapper.MapperInterceptor;
import cn.mybatisboost.metric.MetricInterceptor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfigureAfter(MybatisAutoConfiguration.class)
@ConditionalOnBean(SqlSessionFactory.class)
@Configuration
@EnableConfigurationProperties(MybatisBoostProperties.class)
@Import(NosqlConfiguration.class)
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
    public MybatisBoostInterceptor mybatisBoostInterceptor(cn.mybatisboost.core.Configuration configuration) {
        MybatisBoostInterceptor mybatisBoostInterceptor = new MybatisBoostInterceptor(configuration);
        mybatisBoostInterceptor.appendPreprocessor(new ParameterMappingsPreprocessor());
        mybatisBoostInterceptor.appendPreprocessor(new ParameterNormalizationPreprocessor());
        if (isMapperEnabled) {
            mybatisBoostInterceptor.appendInterceptor
                    (StatementHandler.class, "prepare", new MapperInterceptor(configuration));
        }
        if (isLangEnabled) {
            mybatisBoostInterceptor.appendInterceptor
                    (StatementHandler.class, "prepare", new LangInterceptor(configuration));
        }
        if (isLimiterEnabled) {
            mybatisBoostInterceptor.appendInterceptor
                    (StatementHandler.class, "prepare", new LimiterInterceptor(configuration));
        }
        if (isMetricEnabled) {
            MetricInterceptor interceptor = new MetricInterceptor(configuration);
            mybatisBoostInterceptor.appendInterceptor(StatementHandler.class, "batch", interceptor);
            mybatisBoostInterceptor.appendInterceptor(StatementHandler.class, "update", interceptor);
            mybatisBoostInterceptor.appendInterceptor(StatementHandler.class, "query", interceptor);
        }
        return mybatisBoostInterceptor;
    }
}
