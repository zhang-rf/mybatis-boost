package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import cn.mybatisboost.core.util.MyBatisUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UpdateEnhancement implements SqlProvider, ConfigurationAware {

    private final static Pattern PATTERN_COLUMN = Pattern.compile("(\\w+).*?\\?");
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

            org.apache.ibatis.session.Configuration configuration = (org.apache.ibatis.session.Configuration)
                    metaObject.getValue("delegate.configuration");
            List<ParameterMapping> parameterMappings;
            if (boundSql.getParameterObject().getClass() == entityType) {
                parameterMappings = MyBatisUtils.getParameterMapping(configuration, properties);
            } else {
                parameterMappings = new ArrayList<>(columns.size() * 2);
                for (int i = 1; i <= columns.size(); i++) {
                    parameterMappings.add(new ParameterMapping.Builder
                            (configuration, "param" + i, Object.class).build());
                }
            }

            if (split.length == 2) {
                parameterMappings = new ArrayList<>(parameterMappings);

                String conditions = split[1];
                int conditionCount = StringUtils.countMatches(conditions, '?');
                if (boundSql.getParameterMappings().size() != conditionCount) {
                    Matcher matcher = PATTERN_COLUMN.matcher(conditions);
                    while (matcher.find()) {
                        String property = EntityUtils.getPropertiesFromColumns
                                (entityType, Collections.singletonList(matcher.group(1)), mapUnderscoreToCamelCase)
                                .stream().findFirst().orElseThrow(NoSuchFieldError::new);
                        parameterMappings.add(new ParameterMapping.Builder(configuration,
                                property, Object.class).build());
                    }
                } else {
                    parameterMappings.addAll(boundSql.getParameterMappings());
                }
                sqlBuilder.append(sql.contains(" WHERE ") ? " WHERE " : " where ").append(conditions);
            }

            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
            metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
        }
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
