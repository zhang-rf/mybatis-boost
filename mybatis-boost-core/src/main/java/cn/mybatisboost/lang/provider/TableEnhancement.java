package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;

public class TableEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (sql.contains("#t")) {
            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            metaObject.setValue("delegate.boundSql.sql", sql.replace("#t",
                    EntityUtils.getTableName(entityType, configuration.getNameAdaptor())));
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
