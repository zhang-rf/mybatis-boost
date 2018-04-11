package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MapperUtils;

public class SelectOrCountAll implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String tableName = EntityUtils.getTableName(MapperUtils.getEntityTypeFromMapper
                        (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.'))),
                configuration.getNameAdaptor());
        metaObject.setValue("delegate.boundSql.sql", (mappedStatement.getId().endsWith("countAll") ?
                "SELECT COUNT(*) FROM " : "SELECT * FROM ") + tableName);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
