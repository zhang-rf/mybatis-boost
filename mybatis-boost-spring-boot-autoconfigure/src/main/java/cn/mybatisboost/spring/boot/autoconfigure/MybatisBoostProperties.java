package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.function.BiConsumer;

@ConfigurationProperties("mybatisboost")
public class MybatisBoostProperties {

    private Class<? extends NameAdaptor> nameAdaptor;
    private boolean multipleDatasource;
    private boolean logSqlAndTime;
    private boolean logSqlParameters;
    private long slowSqlThresholdInMillis = Long.MAX_VALUE;
    private Class<? extends BiConsumer<String, Long>> slowSqlHandler;

    public Class<? extends NameAdaptor> getNameAdaptor() {
        return nameAdaptor;
    }

    public void setNameAdaptor(Class<? extends NameAdaptor> nameAdaptor) {
        this.nameAdaptor = nameAdaptor;
    }

    public boolean isMultipleDatasource() {
        return multipleDatasource;
    }

    public void setMultipleDatasource(boolean multipleDatasource) {
        this.multipleDatasource = multipleDatasource;
    }

    public boolean isLogSqlAndTime() {
        return logSqlAndTime;
    }

    public MybatisBoostProperties setLogSqlAndTime(boolean logSqlAndTime) {
        this.logSqlAndTime = logSqlAndTime;
        return this;
    }

    public long getSlowSqlThresholdInMillis() {
        return slowSqlThresholdInMillis;
    }

    public MybatisBoostProperties setSlowSqlThresholdInMillis(long slowSqlThresholdInMillis) {
        this.slowSqlThresholdInMillis = slowSqlThresholdInMillis;
        return this;
    }

    public Class<? extends BiConsumer<String, Long>> getSlowSqlHandler() {
        return slowSqlHandler;
    }

    public MybatisBoostProperties setSlowSqlHandler(Class<? extends BiConsumer<String, Long>> slowSqlHandler) {
        this.slowSqlHandler = slowSqlHandler;
        return this;
    }

    public boolean isLogSqlParameters() {
        return logSqlParameters;
    }

    public MybatisBoostProperties setLogSqlParameters(boolean logSqlParameters) {
        this.logSqlParameters = logSqlParameters;
        return this;
    }
}
