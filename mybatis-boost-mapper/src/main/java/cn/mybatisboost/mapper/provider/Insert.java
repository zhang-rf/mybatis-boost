package cn.mybatisboost.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.core.util.PropertyUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.*;

public class Insert implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map<?, ?> parameterMap = (Map<?, ?>) boundSql.getParameterObject();
        Object entity = parameterMap.get("param1");
        Collection<?> entities;
        if (entity instanceof Collection) {
            entities = (Collection<?>) entity;
            if (entities.isEmpty()) return;
            entity = entities.iterator().next();
        } else {
            entities = Collections.singletonList(entity);
        }

        boolean selective = mappedStatement.getId().endsWith("Selectively");
        String[] candidateProperties = (String[]) parameterMap.get("param2");
        List<String> properties;
        if (candidateProperties.length == 0) {
            properties = EntityUtils.getProperties(entity, selective);
        } else {
            properties = PropertyUtils.buildPropertiesWithCandidates(candidateProperties, entity, selective);
        }

        List<ParameterMapping> parameterMappings = Collections.emptyList();
        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);

            StringBuilder subSqlBuilder = new StringBuilder();
            columns.forEach(property -> subSqlBuilder.append(property).append(", "));
            subSqlBuilder.setLength(subSqlBuilder.length() - 2);
            sqlBuilder.append('(').append(subSqlBuilder).append(") VALUES ");

            subSqlBuilder.setLength(0);
            for (int n = 0; n < entities.size(); n++) {
                subSqlBuilder.append('(');
                for (int i = 0, size = columns.size(); i < size; i++) {
                    subSqlBuilder.append("?, ");
                }
                subSqlBuilder.setLength(subSqlBuilder.length() - 2);
                subSqlBuilder.append("), ");
            }
            subSqlBuilder.setLength(subSqlBuilder.length() - 2);
            sqlBuilder.append(subSqlBuilder);

            if (entities.size() > 1) {
                entity = Collections.singletonMap("collection", entities);
                parameterMappings = new ArrayList<>(properties.size() * entities.size());

                for (int i = 0; i < entities.size(); i++) {
                    org.apache.ibatis.session.Configuration configuration =
                            (org.apache.ibatis.session.Configuration)
                                    metaObject.getValue("delegate.configuration");
                    for (String property : properties) {
                        parameterMappings.add(new ParameterMapping.Builder(configuration,
                                "collection[" + i + "]." + property, Object.class).build());
                    }
                }
            } else {
                parameterMappings = MyBatisUtils.getParameterMappings((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
            }
        }
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
