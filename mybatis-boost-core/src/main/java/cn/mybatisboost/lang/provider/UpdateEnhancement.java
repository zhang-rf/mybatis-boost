package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import cn.mybatisboost.util.MyBatisUtils;
import cn.mybatisboost.util.SqlUtils;
import cn.mybatisboost.util.tuple.BinaryTuple;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE &&
                sql.toUpperCase().startsWith("UPDATE SET ")) {
            String[] split = splitSql(sql); // split[0] = columns, split[1] = conditions(if there were)
            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");

            BinaryTuple<List<String>, List<String>> propertiesAndColumns =
                    SqlUtils.getPropertiesAndColumnsFromLiteralColumns(split[0], entityType, mapUnderscoreToCamelCase);
            List<String> conditionProperties = getConditionProperties(entityType, boundSql.getParameterMappings());
            propertiesAndColumns.first().removeAll(conditionProperties);
            propertiesAndColumns.second().removeAll(conditionProperties.stream()
                    .map(it -> SqlUtils.normalizeColumn(it, mapUnderscoreToCamelCase)).collect(Collectors.toList()));

            metaObject.setValue("delegate.boundSql.sql",
                    buildSQL(sql, entityType, propertiesAndColumns.second(), split));
            metaObject.setValue("delegate.boundSql.parameterMappings",
                    getParameterMappings(metaObject, boundSql, propertiesAndColumns.first()));
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private String[] splitSql(String sql) {
        String[] split = {sql.substring(11)};
        if (split[0].contains(" where ")) {
            split = split[0].split(" where ", 2);
        } else if (split[0].contains(" WHERE ")) {
            split = split[0].split(" WHERE ", 2);
        }
        return split;
    }

    private List<String> getConditionProperties(Class<?> entityType, List<ParameterMapping> parameterMappings) {
        List<String> conditionProperties = parameterMappings.stream()
                .map(ParameterMapping::getProperty).collect(Collectors.toList());
        try {
            String idProperty = EntityUtils.getIdProperty(entityType);
            if (!conditionProperties.contains(idProperty)) {
                conditionProperties.add(idProperty);
            }
        } catch (NoSuchFieldError ignored) {
        }
        return conditionProperties;
    }

    private String buildSQL(String sql, Class<?> entityType, List<String> columns, String[] split) {
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

    private List<ParameterMapping> getParameterMappings(MetaObject metaObject, BoundSql boundSql, List<String> properties) {
        org.apache.ibatis.session.Configuration configuration = (org.apache.ibatis.session.Configuration)
                metaObject.getValue("delegate.configuration");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof Map) {
            for (int i = 1; i <= properties.size(); i++) {
                parameterMappings.add(i - 1,
                        new ParameterMapping.Builder(configuration, "param" + i, Object.class).build());
            }
        } else {
            parameterMappings.addAll(0, MyBatisUtils.getParameterMappings(configuration, properties));
        }
        return parameterMappings;
    }
}
