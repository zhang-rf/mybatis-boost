package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MyBatisUtils;

import java.util.List;

public class Insert implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = "INSERT INTO %s (%s) VALUES (%s)";
        Object parameterObject = boundSql.getParameterObject();
        Class<?> parameterType = parameterObject.getClass();
        String table = EntityUtils.getTableName(parameterType, configuration.getNameAdaptor()), column = "", value = "";

        boolean selective = mappedStatement.getId().endsWith("Selective");
        List<String> properties = EntityUtils.getProperties(parameterObject, selective);
        if (!properties.isEmpty()) {
            boolean mapUnderscoreToCamelCase = (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
            List<String> columns = EntityUtils.getColumns(parameterType, properties, mapUnderscoreToCamelCase);

            StringBuilder builder = new StringBuilder();
            columns.forEach(property -> builder.append(property).append(", "));
            builder.setLength(builder.length() - 2);
            column = builder.toString();

            builder.setLength(0);
            for (int i = 0, size = columns.size(); i < size; i++) {
                builder.append("?, ");
            }
            builder.setLength(builder.length() - 2);
            value = builder.toString();

            List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                    ((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"), properties);
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        }
        metaObject.setValue("delegate.boundSql.sql", String.format(sql, table, column, value));
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
