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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UpdateEnhancement implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE &&
                sql.toUpperCase().startsWith("UPDATE SET ")) {
            String[] split = {sql.substring(11)};
            if (split[0].contains(" where ")) {
                split = split[0].split(" where ", 2);
            } else if (split[0].contains(" WHERE ")) {
                split = split[0].split(" WHERE ", 2);
            }

            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("UPDATE ")
                    .append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor())).append(" SET ");

            List<String> properties, columns;
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            if (Objects.equals(split[0], "*")) {
                properties = EntityUtils.getProperties(entityType);
                columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
            } else {
                if (split[0].toUpperCase().startsWith("NOT ")) {
                    properties = EntityUtils.getProperties(entityType);
                    properties.removeAll(EntityUtils.getPropertiesFromColumns
                            (entityType, Arrays.stream(split[0].substring(4).split(","))
                                    .map(String::trim).collect(Collectors.toList()), mapUnderscoreToCamelCase));
                    columns = EntityUtils.getColumns(entityType, properties, mapUnderscoreToCamelCase);
                } else {
                    columns = Arrays.stream(split[0].split(",")).map(String::trim).collect(Collectors.toList());
                    properties = EntityUtils.getPropertiesFromColumns(entityType, columns, mapUnderscoreToCamelCase);
                }
            }

            columns.forEach(c -> sqlBuilder.append(c).append(" = ?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);

            List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                    ((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
            if (split.length == 2) {
                parameterMappings = new ArrayList<>(parameterMappings);
                parameterMappings.addAll(boundSql.getParameterMappings());
                sqlBuilder.append(sql.contains(" WHERE ") ? " WHERE " : " where ").append(split[1]);
            }

            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
            metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
        }
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
