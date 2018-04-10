package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.*;

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

        Map parameterMap = (Map) boundSql.getParameterObject();
        int parameterLength = parameterMap.size() / 2;

        Object entity = parameterMap.get("arg0");
        boolean isSelectiveUpdating = mappedStatement.getId().endsWith("Selectively");
        List<String> properties;
        if (parameterLength == 2) {
            properties = EntityUtils.getProperties(entity, isSelectiveUpdating);
        } else {
            String[] candidateProperties = (String[]) parameterMap.get("arg1");
            properties = PropertyUtils.buildPropertiesWithCandidates(candidateProperties, entity, isSelectiveUpdating);
        }

        String[] conditionalProperties = (String[]) parameterMap.get(parameterLength == 2 ? "arg1" : "arg2");
        PropertyUtils.rebuildPropertiesWithConditions(properties, entityType, conditionalProperties);

        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
            SqlUtils.appendSet(sqlBuilder, columns.stream().limit(columns.size() - conditionalProperties.length));
            SqlUtils.appendWhere(sqlBuilder, columns.stream().skip(columns.size() - conditionalProperties.length));
        }

        List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                ((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
