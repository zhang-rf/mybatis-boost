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

import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InsertEnhancement implements SqlProvider, ConfigurationAware {

    private static final Pattern PATTERN_LITERAL_COLUMNS = Pattern.compile("(((NOT|not) )?(\\w+, ?)*\\w+|\\*)");
    private Configuration configuration;

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        String sqlUpperCase = sql.toUpperCase();
        if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT &&
                !sqlUpperCase.startsWith("INSERT INTO ") && sqlUpperCase.startsWith("INSERT ")) {
            Matcher matcher = PATTERN_LITERAL_COLUMNS.matcher(sql = sql.substring(7));
            if (!matcher.find()) {
                throw new IllegalStateException("Found INSERT statement but no column is specified");
            }

            String literalColumns = matcher.group();
            String additionalStatement = sql.substring(literalColumns.length());
            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> properties, columns;
            if (Objects.equals(literalColumns, "*") || literalColumns.toUpperCase().startsWith("NOT ")) {
                properties = EntityUtils.getProperties(entityType);
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

            List<?> entities = boundSql.getParameterObject() instanceof Map ?
                    (List<?>) ((Map) boundSql.getParameterObject()).get("param1") :
                    Collections.singletonList(boundSql.getParameterObject());
            if (entities.isEmpty()) return;

            Object parameterObject = getParameterObject(entities);
            List<ParameterMapping> parameterMappings;
            if (entities.size() > 1) {
                parameterMappings = buildParameterMappings(metaObject, properties, entities);
            } else {
                parameterMappings = MyBatisUtils.getParameterMappings((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
            }

            metaObject.setValue("delegate.parameterHandler.parameterObject", parameterObject);
            metaObject.setValue("delegate.boundSql.parameterObject", parameterObject);
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
            metaObject.setValue("delegate.boundSql.sql",
                    buildSql(entityType, columns, entities.size(), additionalStatement));
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private Object getParameterObject(List<?> entities) {
        if (entities.size() > 1) {
            return Collections.singletonMap("list", entities);
        } else {
            return entities.iterator().next();
        }
    }

    private List<ParameterMapping> buildParameterMappings
            (MetaObject metaObject, List<String> properties, List<?> entities) {
        List<ParameterMapping> parameterMappings = new ArrayList<>(properties.size() * entities.size());
        org.apache.ibatis.session.Configuration configuration =
                (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");
        for (int i = 0; i < entities.size(); i++) {
            for (String property : properties) {
                parameterMappings.add(new ParameterMapping.Builder
                        (configuration, "list[" + i + "]." + property, Object.class).build());
            }
        }
        return parameterMappings;
    }

    private String buildSql(Class<?> entityType, List<String> columns, int batchSize, String additionalStatement) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ")
                .append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));
        sqlBuilder.append(" (");
        columns.forEach(c -> sqlBuilder.append(c).append(", "));
        sqlBuilder.setLength(sqlBuilder.length() - 2);
        sqlBuilder.append(") VALUES ");
        for (int i = 0; i < batchSize; i++) {
            sqlBuilder.append("(");
            columns.forEach(c -> sqlBuilder.append("?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append("), ");
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
        sqlBuilder.append(additionalStatement);
        return sqlBuilder.toString();
    }
}
