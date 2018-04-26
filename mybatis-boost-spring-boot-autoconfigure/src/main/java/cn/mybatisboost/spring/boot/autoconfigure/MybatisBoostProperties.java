package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.function.BiConsumer;

@ConfigurationProperties("mybatisboost")
public class MybatisBoostProperties {

    private Class<? extends NameAdaptor> nameAdaptor;
    private boolean multipleDatasource;
    private boolean showQuery;
    private boolean showQueryWithParameters;
    private long slowQueryThresholdInMillis = Long.MAX_VALUE;
    private Class<? extends BiConsumer<String, Long>> slowQueryHandler;

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

    public boolean isShowQuery() {
        return showQuery;
    }

    public MybatisBoostProperties setShowQuery(boolean showQuery) {
        this.showQuery = showQuery;
        return this;
    }

    public boolean isShowQueryWithParameters() {
        return showQueryWithParameters;
    }

    public MybatisBoostProperties setShowQueryWithParameters(boolean showQueryWithParameters) {
        this.showQueryWithParameters = showQueryWithParameters;
        return this;
    }

    public long getSlowQueryThresholdInMillis() {
        return slowQueryThresholdInMillis;
    }

    public MybatisBoostProperties setSlowQueryThresholdInMillis(long slowQueryThresholdInMillis) {
        this.slowQueryThresholdInMillis = slowQueryThresholdInMillis;
        return this;
    }

    public Class<? extends BiConsumer<String, Long>> getSlowQueryHandler() {
        return slowQueryHandler;
    }

    public MybatisBoostProperties setSlowQueryHandler(Class<? extends BiConsumer<String, Long>> slowQueryHandler) {
        this.slowQueryHandler = slowQueryHandler;
        return this;
    }
}
