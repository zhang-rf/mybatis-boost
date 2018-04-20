package cn.mybatisboost.core;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;

import java.util.function.BiConsumer;

public class Configuration {

    private NameAdaptor nameAdaptor = new NoopNameAdaptor();
    private boolean multipleDatasource;
    private boolean logSqlAndTime = false;
    private long slowSqlThresholdInMillis;
    private BiConsumer<String, Long> slowSqlHandler;

    public static Builder builder() {
        return new Builder();
    }

    public NameAdaptor getNameAdaptor() {
        return nameAdaptor;
    }

    public boolean isMultipleDatasource() {
        return multipleDatasource;
    }

    public boolean isLogSqlAndTime() {
        return logSqlAndTime;
    }

    public long getSlowSqlThresholdInMillis() {
        return slowSqlThresholdInMillis;
    }

    public BiConsumer<String, Long> getSlowSqlHandler() {
        return slowSqlHandler;
    }

    public static class Builder {

        private Configuration configuration = new Configuration();

        public Configuration build() {
            return configuration;
        }

        public Builder setNameAdaptor(NameAdaptor nameAdaptor) {
            configuration.nameAdaptor = nameAdaptor;
            return this;
        }

        public Builder setMultipleDatasource(boolean multipleDatasource) {
            configuration.multipleDatasource = multipleDatasource;
            return this;
        }

        public Builder setLogSqlAndTime(boolean logSqlAndTime) {
            configuration.logSqlAndTime = logSqlAndTime;
            return this;
        }

        public Builder setSlowSqlThresholdInMillis(long slowSqlThresholdInMillis) {
            configuration.slowSqlThresholdInMillis = slowSqlThresholdInMillis;
            return this;
        }

        public Builder setSlowSqlHandler(BiConsumer<String, Long> slowSqlHandler) {
            configuration.slowSqlHandler = slowSqlHandler;
            return this;
        }
    }
}
