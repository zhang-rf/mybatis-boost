package me.rfprojects.core;

import me.rfprojects.core.adaptor.NameAdaptor;
import me.rfprojects.core.adaptor.NoopNameAdaptor;

public class Configuration {

    private NameAdaptor nameAdaptor = new NoopNameAdaptor();
    private boolean singleDatasource = true;

    public static Builder builder() {
        return new Builder();
    }

    public NameAdaptor getNameAdaptor() {
        return nameAdaptor;
    }

    public boolean isSingleDatasource() {
        return singleDatasource;
    }

    public static class Builder {

        private Configuration configuration = new Configuration();

        public Builder setNameAdaptor(NameAdaptor nameAdaptor) {
            configuration.nameAdaptor = nameAdaptor;
            return this;
        }

        public Builder setSingleDatasource(boolean singleDatasource) {
            configuration.singleDatasource = singleDatasource;
            return this;
        }

        public Configuration build() {
            return configuration;
        }
    }
}
