package cn.mybatisboost.core.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.List;
import java.util.Map;

public class Update implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map<?, ?> parameterMap = (Map<?, ?>) boundSql.getParameterObject();
        int parameterCount = parameterMap.size() / 2;

        Object entity = parameterMap.get("param1");
        boolean selective = mappedStatement.getId().endsWith("Selectively");
        List<String> properties;
        if (parameterCount == 2) {
            properties = EntityUtils.getProperties(entity, selective);
        } else {
            String[] candidateProperties = (String[]) parameterMap.get("param2");
            properties = PropertyUtils.buildPropertiesWithCandidates(candidateProperties, entity, selective);
        }

        String[] conditionalProperties;
        if (parameterCount == 3) {
            conditionalProperties = (String[]) parameterMap.get("param3");
        } else {
            conditionalProperties = new String[]{EntityUtils.getIdProperty(entityType)};
        }
        PropertyUtils.rebuildPropertiesWithConditions(properties, entityType, conditionalProperties);

        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
            sqlBuilder.append(" SET ");
            columns.stream().limit(columns.size() - conditionalProperties.length)
                    .forEach(c -> sqlBuilder.append(c).append(" = ?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            SqlUtils.appendWhere(sqlBuilder, columns.stream().skip(columns.size() - conditionalProperties.length));
        }

        List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                ((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
        metaObject.setValue("delegate.parameterHandler.parameterObject", entity);
        metaObject.setValue("delegate.boundSql.parameterObject", entity);
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
