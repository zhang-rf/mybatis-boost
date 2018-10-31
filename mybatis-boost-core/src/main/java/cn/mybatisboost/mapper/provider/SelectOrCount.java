package cn.mybatisboost.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import cn.mybatisboost.util.MyBatisUtils;
import cn.mybatisboost.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectOrCount implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void handle(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(mappedStatement.getId().endsWith("count") ? "SELECT COUNT(*) FROM " : "SELECT * FROM ")
                .append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map<?, ?> parameterMap = (Map<?, ?>) boundSql.getParameterObject();
        Object entity = parameterMap.get("param1");
        List<String> properties;
        String[] conditionalProperties = (String[]) (parameterMap.containsKey("param2") ?
                parameterMap.get("param2") : parameterMap.get("param3"));
        if (conditionalProperties.length == 0) {
            properties = EntityUtils.getProperties(entity, true);
        } else {
            properties = Arrays.asList(conditionalProperties);
        }

        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = properties.stream()
                    .map(it -> SqlUtils.normalizeColumn(it, mapUnderscoreToCamelCase)).collect(Collectors.toList());
            SqlUtils.appendWhere(sqlBuilder, columns.stream());
        }

        List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMappings
                ((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), properties);
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
