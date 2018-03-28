package me.rfprojects.mapper.provider;

import me.rfprojects.core.Configuration;
import me.rfprojects.core.ConfigurationAware;
import me.rfprojects.core.SqlProvider;
import me.rfprojects.core.util.EntityUtils;
import me.rfprojects.core.util.MyBatisUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.List;

public class Update implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        Class<?> parameterType = parameterObject.getClass();

        String tableName = EntityUtils.getTableName(parameterType, configuration.getNameAdaptor());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(tableName);

        boolean selective = mappedStatement.getId().endsWith("Selective");
        List<String> properties = EntityUtils.getProperties(parameterObject, selective);
        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(parameterType, properties, mapUnderscoreToCamelCase);

            List<Integer> idIndexes = EntityUtils.getIdIndexes(parameterType, properties);
            List<String> ids = new ArrayList<>();
            idIndexes.forEach(i -> ids.add(columns.get(i)));
            columns.removeAll(ids);

            sqlBuilder.append(" SET ");
            columns.forEach(property -> sqlBuilder.append(property).append(" = ?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(" WHERE ");
            ids.forEach(id -> sqlBuilder.append(id).append(" = ?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);

            columns.addAll(ids);
            List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                    ((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        }
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
