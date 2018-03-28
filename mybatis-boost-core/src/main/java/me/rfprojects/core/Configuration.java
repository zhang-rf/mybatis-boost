package me.rfprojects.core;

import me.rfprojects.core.adaptor.NameAdaptor;
import me.rfprojects.core.adaptor.NoopNameAdaptor;

public class Configuration {

    private NameAdaptor nameAdaptor = new NoopNameAdaptor();
    private boolean multipleDatasource;

    public static Builder builder() {
        return new Builder();
    }

    public NameAdaptor getNameAdaptor() {
        return nameAdaptor;
    }

    public boolean isMultipleDatasource() {
        return multipleDatasource;
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
    }
}
