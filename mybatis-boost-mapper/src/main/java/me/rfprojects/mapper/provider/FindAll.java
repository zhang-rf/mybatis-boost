package me.rfprojects.mapper.provider;

import me.rfprojects.core.Configuration;
import me.rfprojects.core.ConfigurationAware;
import me.rfprojects.core.SqlProvider;
import me.rfprojects.core.util.EntityUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

public class FindAll implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String tableName = EntityUtils.getTableName
                (mappedStatement.getResultMaps().get(0).getType(), configuration.getNameAdaptor());
        metaObject.setValue("delegate.boundSql.sql", "SELECT * FROM " + tableName);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
