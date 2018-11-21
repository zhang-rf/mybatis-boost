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
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertEnhancement implements SqlProvider, ConfigurationAware {

    private static final Pattern PATTERN_LITERAL_COLUMNS = Pattern.compile("((NOT|not) )?(\\w+, ?)*\\w+|\\*");
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
            Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                    (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            BinaryTuple<List<String>, List<String>> propertiesAndColumns =
                    SqlUtils.getPropertiesAndColumnsFromLiteralColumns(literalColumns, entityType, mapUnderscoreToCamelCase);

            List<?> entities = boundSql.getParameterObject() instanceof Map ?
                    (List<?>) ((Map) boundSql.getParameterObject()).get("param1") :
                    Collections.singletonList(Objects.requireNonNull(boundSql.getParameterObject(),
                            "ParameterObject mustn't be null"));
            if (entities.isEmpty()) {
                throw new IllegalArgumentException("Can't insert empty list");
            } else {
                String additionalStatement = sql.substring(literalColumns.length());
                org.apache.ibatis.session.Configuration configuration =
                        (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");
                Object parameterObject = buildParameterObject(entities);
                metaObject.setValue("delegate.boundSql.sql",
                        buildSql(entityType, propertiesAndColumns.second(), entities.size(), additionalStatement));
                metaObject.setValue("delegate.boundSql.parameterMappings",
                        MyBatisUtils.getListParameterMappings(configuration, propertiesAndColumns.first(), entities.size()));
                metaObject.setValue("delegate.boundSql.parameterObject", parameterObject);
                metaObject.setValue("delegate.parameterHandler.parameterObject", parameterObject);
            }
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private Object buildParameterObject(List<?> entities) {
        Map<String, Object> map = new HashMap<>();
        map.put("list", entities);
        map.put("collection", entities);
        return map;
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
