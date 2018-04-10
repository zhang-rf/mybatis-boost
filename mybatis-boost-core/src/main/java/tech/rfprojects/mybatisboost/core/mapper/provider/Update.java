package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.mapper.GenericMapper;
import tech.rfprojects.mybatisboost.core.util.*;

import java.util.List;
import java.util.Map;

public class Update implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        boolean isSelectiveUpdating = mappedStatement.getId().endsWith("Selectively");
        Map parameterMap = (Map) boundSql.getParameterObject();
        int parameterLength = parameterMap.size() / 2;

        Class<?> entityType = MapperUtils.getEntityTypeFromMapper(GenericMapper.class,
                mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        String tableName = EntityUtils.getTableName(entityType, configuration.getNameAdaptor());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(tableName);

        List<String> properties;
        if (parameterLength == 2) {
            properties = EntityUtils.getProperties(parameterMap.get("arg0"), isSelectiveUpdating);
        } else {
            Object parameterObject = parameterMap.get("arg0");
            String[] candidateProperties = (String[]) parameterMap.get("arg1");
            properties = PropertyUtils.buildPropertiesWithCandidates
                    (candidateProperties, parameterObject, isSelectiveUpdating);
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
