package cn.mybatisboost.metric;

import cn.mybatisboost.core.Configuration;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})})
public class MetricInterceptor implements Interceptor {

    private static Logger logger = LoggerFactory.getLogger(MetricInterceptor.class);
    private Configuration configuration;

    public MetricInterceptor() {
        this(new Configuration());
    }

    public MetricInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StopWatch stopWatch = StopWatch.createStarted();
        Object proceed = invocation.proceed();
        BoundSql boundSql = ((StatementHandler) invocation.getTarget()).getBoundSql();

        List<Object> parameters = new ArrayList<>();
        if (configuration.isLogSqlParameters()) {
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            Object parameterObject = boundSql.getParameterObject();
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            if (parameterMappings.size() == 1 && !(parameterObject instanceof Map) &&
                    !metaObject.hasGetter(parameterMappings.get(0).getProperty())) {
                parameters.add(parameterObject);
            } else {
                parameterMappings.forEach(pm -> parameters.add(metaObject.getValue(pm.getProperty())));
            }
        }

        String sql = boundSql.getSql().replaceAll("\\s*\\n\\s*", " ");
        long time = stopWatch.getTime();
        if (time > configuration.getSlowSqlThresholdInMillis()) {
            if (parameters.isEmpty()) {
                logger.error(String.format("[SLOW Query took %s ms] %s", time, sql));
            } else {
                logger.error(String.format("[SLOW Query took %s ms, Parameters: %s] %s ", time, parameters, sql));
            }
            BiConsumer<String, Long> slowSqlHandler = configuration.getSlowSqlHandler();
            if (slowSqlHandler != null) {
                slowSqlHandler.accept(sql, time);
            }
        } else if (configuration.isLogSqlAndTime()) {
            if (parameters.isEmpty()) {
                logger.info(String.format("[Query took %s ms] %s", time, sql));
            } else {
                logger.info(String.format("[Query took %s ms, Parameters: %s] %s ", time, parameters, sql));
            }
        }
        return proceed;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
