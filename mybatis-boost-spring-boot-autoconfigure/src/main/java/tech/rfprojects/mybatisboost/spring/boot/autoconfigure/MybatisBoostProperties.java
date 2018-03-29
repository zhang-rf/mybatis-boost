package tech.rfprojects.mybatisboost.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tech.rfprojects.mybatisboost.core.adaptor.NameAdaptor;

@ConfigurationProperties("mybatisboost")
public class MybatisBoostProperties {

    private boolean multipleDatasource;
    private Class<? extends NameAdaptor> nameAdaptor;

    public boolean isMultipleDatasource() {
        return multipleDatasource;
    }

    public void setMultipleDatasource(boolean multipleDatasource) {
        this.multipleDatasource = multipleDatasource;
    }

    public Class<? extends NameAdaptor> getNameAdaptor() {
        return nameAdaptor;
    }

    public void setNameAdaptor(Class<? extends NameAdaptor> nameAdaptor) {
        this.nameAdaptor = nameAdaptor;
    }
}
