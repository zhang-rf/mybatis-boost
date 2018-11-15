package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.DispatcherInterceptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.core.preprocessor.AutoParameterMappingPreprocessor;
import cn.mybatisboost.core.preprocessor.MybatisCacheRemovingPreprocessor;
import cn.mybatisboost.core.preprocessor.ParameterNormalizationPreprocessor;
import cn.mybatisboost.lang.LanguageSqlProvider;
import cn.mybatisboost.limiter.LimiterSqlProvider;
import cn.mybatisboost.mapper.MapperSqlProvider;
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
    @Value("${mybatisboost.metric.enabled:false}")
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
    public DispatcherInterceptor mybatisBoostInterceptor(cn.mybatisboost.core.Configuration configuration) {
        DispatcherInterceptor dispatcherInterceptor = new DispatcherInterceptor(configuration);
        dispatcherInterceptor.appendPreprocessor(new MybatisCacheRemovingPreprocessor());
        dispatcherInterceptor.appendPreprocessor(new ParameterNormalizationPreprocessor());
        dispatcherInterceptor.appendPreprocessor(new AutoParameterMappingPreprocessor());
        if (isMapperEnabled) {
            dispatcherInterceptor.appendProvider(new MapperSqlProvider(configuration));
        }
        if (isLangEnabled) {
            dispatcherInterceptor.appendProvider(new LanguageSqlProvider(configuration));
        }
        if (isLimiterEnabled) {
            dispatcherInterceptor.appendProvider(new LimiterSqlProvider(configuration));
        }
        return dispatcherInterceptor;
    }
}
