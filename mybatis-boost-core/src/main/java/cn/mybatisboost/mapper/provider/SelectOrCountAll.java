package cn.mybatisboost.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;

public class SelectOrCountAll implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void handle(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
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
