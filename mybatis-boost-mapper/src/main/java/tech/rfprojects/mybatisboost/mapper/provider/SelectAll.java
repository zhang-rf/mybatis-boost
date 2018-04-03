package tech.rfprojects.mybatisboost.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;

public class SelectAll implements SqlProvider, ConfigurationAware {

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
