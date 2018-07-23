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

import java.util.*;
import java.util.stream.Collectors;

public class InsertEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

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
                sqlBuilder.append("INSERT INTO ")
                        .append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

                Collection<?> entities = boundSql.getParameterObject() instanceof Map ?
                        (Collection<?>) ((Map) boundSql.getParameterObject()).get("param1") :
                        Collections.singleton(boundSql.getParameterObject());
                if (entities.isEmpty()) return;

                List<String> properties, columns;
                boolean mapUnderscoreToCamelCase = (boolean)
                        metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
                if (Objects.equals(split[1], "*")) {
                    properties = EntityUtils.getProperties(entityType);
                    columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
                } else {
                    if (split[1].toUpperCase().startsWith("NOT ")) {
                        properties = EntityUtils.getProperties(entityType);
                        properties.removeAll(EntityUtils.getPropertiesFromColumns(entityType,
                                Arrays.stream(split[1].substring(4).split(","))
                                        .map(String::trim).collect(Collectors.toList()), mapUnderscoreToCamelCase));
                        columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
                    } else {
                        columns = Arrays.stream(split[1].split(","))
                                .map(String::trim).collect(Collectors.toList());
                        properties = EntityUtils.getPropertiesFromColumns
                                (entityType, columns, mapUnderscoreToCamelCase);
                    }
                }

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

                Object entity;
                List<ParameterMapping> parameterMappings;
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
                    entity = entities.iterator().next();
                    parameterMappings = MyBatisUtils.getParameterMapping((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
                }

                metaObject.setValue("delegate.parameterHandler.parameterObject", entity);
                metaObject.setValue("delegate.boundSql.parameterObject", entity);
                metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
                metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
            }
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
