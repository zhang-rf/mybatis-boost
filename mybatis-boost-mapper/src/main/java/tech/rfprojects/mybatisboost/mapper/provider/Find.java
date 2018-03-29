package tech.rfprojects.mybatisboost.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MyBatisUtils;
import tech.rfprojects.mybatisboost.core.util.SqlUtils;

import java.util.List;

public class Find implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        Class<?> parameterType = parameterObject.getClass();

        String tableName = EntityUtils.getTableName(parameterType, configuration.getNameAdaptor());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);

        List<String> properties = EntityUtils.getProperties(parameterObject, true);
        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(parameterType, properties, mapUnderscoreToCamelCase);

            SqlUtils.appendWhere(sqlBuilder, columns);
            List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                    ((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        }
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
