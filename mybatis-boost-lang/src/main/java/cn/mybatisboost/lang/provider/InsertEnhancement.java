package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import cn.mybatisboost.core.util.MyBatisUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class InsertEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @SuppressWarnings("unchecked")
    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT &&
                !sql.toUpperCase().startsWith("INSERT INTO")) {
            String[] split = sql.split(" ", 2);
            if (split.length == 2 && "INSERT".equalsIgnoreCase(split[0])) {
                Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                        (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO ").append(EntityUtils.getTableName
                        (entityType, configuration.getNameAdaptor()));

                Map parameterMap = (Map) boundSql.getParameterObject();
                Object entity = parameterMap.get("arg0");
                List<?> entities;
                if (entity instanceof List) {
                    entities = (List<?>) entity;
                } else {
                    entities = Collections.singletonList(entity);
                }

                List<String> properties;
                if (Objects.equals(split[1], "*")) {
                    properties = EntityUtils.getProperties(entityType);
                } else {
                    if (split[1].toUpperCase().startsWith("NOT ")) {
                        properties = EntityUtils.getProperties(entityType);
                        properties.removeAll(Arrays.stream(split[1].substring(4).split(","))
                                .map(String::trim).collect(Collectors.toList()));
                    } else {
                        properties = Arrays.stream(split[1].split(","))
                                .map(String::trim).collect(Collectors.toList());
                    }
                }

                boolean mapUnderscoreToCamelCase = (boolean)
                        metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
                List<String> columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);

                sqlBuilder.append(" (");
                columns.forEach(c -> sqlBuilder.append(c).append(", "));
                sqlBuilder.setLength(sqlBuilder.length() - 2);
                sqlBuilder.append(") VALUES ");
                for (int i = 0, size = entities.size(); i < size; i++) {
                    sqlBuilder.append("(");
                    columns.forEach(c -> sqlBuilder.append("?, "));
                    sqlBuilder.setLength(sqlBuilder.length() - 2);
                    sqlBuilder.append("), ");
                }
                sqlBuilder.setLength(sqlBuilder.length() - 2);

                List<ParameterMapping> parameterMappings;
                if (entities.size() > 1) {
                    parameterMap = new HashMap<>(properties.size() * entities.size());
                    parameterMappings = new ArrayList<>(properties.size() * entities.size());
                    for (int i = 0; i < entities.size(); i++) {
                        try {
                            PropertyDescriptor[] descriptors =
                                    Introspector.getBeanInfo(entityType).getPropertyDescriptors();
                            for (String property : properties) {
                                PropertyDescriptor descriptor = Arrays.stream(descriptors)
                                        .filter(d -> Objects.equals(d.getName(), property))
                                        .findAny().orElseThrow(NoSuchFieldError::new);
                                parameterMap.put(property + i, descriptor.getReadMethod().invoke(entities.get(i)));
                            }
                        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                        org.apache.ibatis.session.Configuration configuration =
                                (org.apache.ibatis.session.Configuration)
                                        metaObject.getValue("delegate.configuration");
                        for (String property : properties) {
                            parameterMappings.add(new ParameterMapping.Builder(configuration,
                                    property + i, Object.class).build());
                        }
                    }
                    metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
                    metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
                } else {
                    parameterMappings = MyBatisUtils.getParameterMapping((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
                }

                metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
                metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
            }
        }
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
