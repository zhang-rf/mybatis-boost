package tech.rfprojects.mybatisboost.metric;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.rfprojects.mybatisboost.core.Configuration;

import java.sql.Statement;
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

        String sql = ((StatementHandler) invocation.getTarget()).getBoundSql().getSql()
                .replaceAll("\\s*\\n\\s*", " ");
        long time = stopWatch.getTime();
        long threshold = configuration.getSlowSqlThresholdInMillis();
        if (threshold > 0 && time > threshold) {
            logger.error(String.format("[SLOW Query took %s ms] %s", time, sql));
            BiConsumer<String, Long> slowSqlHandler = configuration.getSlowSqlHandler();
            if (slowSqlHandler != null) {
                slowSqlHandler.accept(sql, time);
            }
        } else if (configuration.isLogSqlAndTime()) {
            logger.info(String.format("[Query took %s ms] %s", time, sql));
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
