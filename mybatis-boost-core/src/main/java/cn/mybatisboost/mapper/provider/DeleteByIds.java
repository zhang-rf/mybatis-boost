package cn.mybatisboost.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import cn.mybatisboost.core.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DeleteByIds implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    @SuppressWarnings("unchecked")
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DELETE FROM ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map<String, Object> parameterMap = (Map<String, Object>) boundSql.getParameterObject();
        Object[] ids = (Object[]) parameterMap.get("param1");
        parameterMap.clear();
        if (ids.length > 0) {
            String idColumn = SqlUtils.normalizeColumn(EntityUtils.getIdProperty(entityType), (boolean)
                    metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase"));
            sqlBuilder.append(" WHERE ").append(idColumn).append(" IN (");
            Arrays.stream(ids).forEach(c -> sqlBuilder.append("?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(')');

            String idProperty = EntityUtils.getIdProperty(entityType);
            org.apache.ibatis.session.Configuration configuration = (org.apache.ibatis.session.Configuration)
                    metaObject.getValue("delegate.configuration");
            List<ParameterMapping> parameterMappings = new ArrayList<>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                parameterMap.put(idProperty + i, ids[i]);
                parameterMappings.add(new ParameterMapping.Builder(configuration,
                        idProperty + i, Object.class).build());
            }
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
            metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
