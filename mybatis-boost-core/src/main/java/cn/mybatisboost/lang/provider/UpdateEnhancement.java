package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE &&
                sql.toUpperCase().startsWith("UPDATE SET ")) {
            String[] split = getSplit(sql); // split[0] = columns, split[1] = conditions(if there were)
            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");

            String literalColumns = split[0];
            List<String> properties, columns;
            if (Objects.equals(literalColumns, "*") || literalColumns.toUpperCase().startsWith("NOT ")) {
                properties = EntityUtils.getProperties(entityType);
                properties.removeAll(getConditionProperties
                        (boundSql.getParameterMappings(), split, entityType));
                if (literalColumns.toUpperCase().startsWith("NOT ")) {
                    properties.removeAll
                            (Arrays.stream(literalColumns.substring(4).split(","))
                                    .map(String::trim).map(PropertyUtils::normalizeProperty)
                                    .collect(Collectors.toList()));
                }
                columns = properties.stream()
                        .map(it -> SqlUtils.normalizeColumn(it, mapUnderscoreToCamelCase)).collect(Collectors.toList());
            } else {
                columns = Arrays.stream(literalColumns.split(",")).map(String::trim).collect(Collectors.toList());
                properties = columns.stream().map(PropertyUtils::normalizeProperty).collect(Collectors.toList());
            }
            metaObject.setValue("delegate.boundSql.parameterMappings",
                    getParameterMappings(metaObject, boundSql, properties, entityType));
            metaObject.setValue("delegate.boundSql.sql", buildUpdateSQL(sql, entityType, columns, split));
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private String[] getSplit(String sql) {
        String[] split = {sql.substring(11)};
        if (split[0].contains(" where ")) {
            split = split[0].split(" where ", 2);
        } else if (split[0].contains(" WHERE ")) {
            split = split[0].split(" WHERE ", 2);
        }
        return split;
    }

    private List<String> getConditionProperties(List<ParameterMapping> parameterMappings,
                                                String[] split, Class<?> entityType) {
        List<String> conditionProperties;
        if (split.length == 2) {
            if (!parameterMappings.isEmpty()) {
                conditionProperties = parameterMappings.stream()
                        .map(ParameterMapping::getProperty).collect(Collectors.toList());
            } else {
                conditionProperties = SqlUtils.findColumnsFromSQL(split[1]).stream()
                        .map(PropertyUtils::normalizeProperty).collect(Collectors.toList());
            }
            String idProperty = EntityUtils.getIdProperty(entityType);
            if (!conditionProperties.contains(idProperty)) {
                if (conditionProperties.getClass() != ArrayList.class) {
                    conditionProperties = new ArrayList<>(conditionProperties);
                }
                conditionProperties.add(idProperty);
            }
        } else {
            conditionProperties = Collections.singletonList(EntityUtils.getIdProperty(entityType));
        }
        return conditionProperties;
    }

    private List<ParameterMapping> getParameterMappings
            (MetaObject metaObject, BoundSql boundSql, List<String> properties, Class<?> entityType) {
        org.apache.ibatis.session.Configuration configuration = (org.apache.ibatis.session.Configuration)
                metaObject.getValue("delegate.configuration");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        if (!parameterMappings.isEmpty() || parameterObject.getClass() == entityType) {
            parameterMappings.addAll(0, MyBatisUtils.getParameterMappings(configuration, properties));
        } else {
            Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
            for (int i = 1; parameterMap.containsKey("param" + i); i++) {
                parameterMappings.add(new ParameterMapping.Builder
                        (configuration, "param" + i, Object.class).build());
            }
        }
        return parameterMappings;
    }

    private String buildUpdateSQL(String sql, Class<?> entityType, List<String> columns, String[] split) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ")
                .append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor())).append(" SET ");
        columns.forEach(c -> sqlBuilder.append(c).append(" = ?, "));
        sqlBuilder.setLength(sqlBuilder.length() - 2);
        if (split.length == 2) {
            sqlBuilder.append(sql.contains(" WHERE ") ? " WHERE " : " where ").append(split[1]);
        }
        return sqlBuilder.toString();
    }
}
