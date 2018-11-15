package cn.mybatisboost.core;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;

import java.util.function.BiConsumer;

public class Configuration {

    private NameAdaptor nameAdaptor = new NoopNameAdaptor();
    private boolean multipleDatasource;
    private boolean iterateSelectiveInBatch;
    private boolean showQuery;
    private boolean showQueryWithParameters;
    private long slowQueryThresholdInMillis = Long.MAX_VALUE;
    private BiConsumer<String, Long> slowQueryHandler;

    public static Builder builder() {
        return new Builder();
    }

    public NameAdaptor getNameAdaptor() {
        return nameAdaptor;
    }

    public boolean isMultipleDatasource() {
        return multipleDatasource;
    }

    public boolean isIterateSelectiveInBatch() {
        return iterateSelectiveInBatch;
    }

    public boolean isShowQuery() {
        return showQuery;
    }

    public boolean isShowQueryWithParameters() {
        return showQueryWithParameters;
    }

    public long getSlowQueryThresholdInMillis() {
        return slowQueryThresholdInMillis;
    }

    public BiConsumer<String, Long> getSlowQueryHandler() {
        return slowQueryHandler;
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

        public Builder setIterateSelectiveInBatch(boolean iterateSelectiveInBatch) {
            configuration.iterateSelectiveInBatch = iterateSelectiveInBatch;
            return this;
        }

        public Builder setShowQuery(boolean showQuery) {
            configuration.showQuery = showQuery;
            return this;
        }

        public Builder setShowQueryWithParameters(boolean showQueryWithParameters) {
            configuration.showQueryWithParameters = showQueryWithParameters;
            return this;
        }

        public Builder setSlowQueryThresholdInMillis(long slowQueryThresholdInMillis) {
            configuration.slowQueryThresholdInMillis = slowQueryThresholdInMillis;
            return this;
        }

        public Builder setSlowQueryHandler(BiConsumer<String, Long> slowQueryHandler) {
            configuration.slowQueryHandler = slowQueryHandler;
            return this;
        }
    }
}
