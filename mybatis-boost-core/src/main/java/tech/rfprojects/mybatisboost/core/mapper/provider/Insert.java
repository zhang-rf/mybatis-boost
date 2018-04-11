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
import tech.rfprojects.mybatisboost.core.util.PropertyUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Insert implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map parameterMap = (Map) boundSql.getParameterObject();
        Object entity = parameterMap.get("arg0");
        List<?> entityList;
        if (entity instanceof List) {
            entityList = (List<?>) entity;
            entity = entityList.get(0);
        } else {
            entityList = Collections.singletonList(entity);
        }

        boolean selective = mappedStatement.getId().endsWith("Selectively");
        String[] candidateProperties = (String[]) parameterMap.get("arg1");
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
            for (int n = 0; n < entityList.size(); n++) {
                subSqlBuilder.append('(');
                for (int i = 0, size = columns.size(); i < size; i++) {
                    subSqlBuilder.append("?, ");
                }
                subSqlBuilder.setLength(subSqlBuilder.length() - 2);
                subSqlBuilder.append("), ");
            }
            subSqlBuilder.setLength(subSqlBuilder.length() - 2);
            sqlBuilder.append(subSqlBuilder);

            if (entityList.size() > 1) {
                Map<String, Object> newParameterMap = new HashMap<>(properties.size() * entityList.size());
                parameterMappings = new ArrayList<>(properties.size() * entityList.size());
                for (int i = 0; i < entityList.size(); i++) {
                    try {
                        Object currentEntity = entityList.get(i);
                        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(entityType).getPropertyDescriptors();
                        for (String property : properties) {
                            PropertyDescriptor descriptor = Arrays.stream(descriptors)
                                    .filter(d -> Objects.equals(d.getName(), property))
                                    .findAny().orElseThrow(NoSuchFieldError::new);
                            newParameterMap.put(property + i, descriptor.getReadMethod().invoke(currentEntity));
                        }
                    } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    for (String property : properties) {
                        parameterMappings.add(new ParameterMapping.Builder((org.apache.ibatis.session.Configuration)
                                metaObject.getValue("delegate.configuration"),
                                property + i, Object.class).build());
                    }
                }
                entity = newParameterMap;
            } else {
                parameterMappings = MyBatisUtils.getParameterMapping((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
            }
        }
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
