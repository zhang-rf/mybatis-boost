package me.rfprojects.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mybatis.boost")
public class MybatisBoostProperties {

    private boolean multipleDatasource;

    public boolean isMultipleDatasource() {
        return multipleDatasource;
    }

    public void setMultipleDatasource(boolean multipleDatasource) {
        this.multipleDatasource = multipleDatasource;
    }
}
