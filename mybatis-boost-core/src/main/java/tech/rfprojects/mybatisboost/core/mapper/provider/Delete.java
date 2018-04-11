package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MapperUtils;
import tech.rfprojects.mybatisboost.core.util.MyBatisUtils;
import tech.rfprojects.mybatisboost.core.util.SqlUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Delete implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DELETE FROM ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map parameterMap = (Map) boundSql.getParameterObject();
        Object entity = parameterMap.get("arg0");
        List<String> properties;
        String[] conditionalProperties = (String[]) parameterMap.get("arg1");
        if (conditionalProperties.length == 0) {
            properties = EntityUtils.getProperties(entity, true);
        } else {
            properties = Arrays.asList(conditionalProperties);
        }

        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
            SqlUtils.appendWhere(sqlBuilder, columns.stream());
        }

        List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                ((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.parameterObject", entity);
        metaObject.setValue("delegate.parameterHandler.parameterObject", entity);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
