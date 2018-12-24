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
import java.util.*;

public class SelectByIds implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        String idProperty = EntityUtils.getIdProperty(entityType);
        String idColumn = SqlUtils.normalizeColumn(idProperty,
                (boolean) metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase"));
        boolean multipleIds = mappedStatement.getId().endsWith("Ids");
        List<ParameterMapping> parameterMappings = Collections.emptyList();
        if (!multipleIds) {
            sqlBuilder.append(" WHERE ").append(idColumn).append(" = ?");
            parameterMappings = Collections.singletonList(new ParameterMapping.Builder
                    ((org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration"),
                            "param1", Object.class).build());
        } else {
            Map<?, ?> parameterMap = (Map<?, ?>) boundSql.getParameterObject();
            Object[] ids = (Object[]) parameterMap.get("param1");
            if (ids.length > 0) {
                sqlBuilder.append(" WHERE ").append(idColumn).append(" IN (");
                Arrays.stream(ids).forEach(c -> sqlBuilder.append("?, "));
                sqlBuilder.setLength(sqlBuilder.length() - 2);
                sqlBuilder.append(')');

                org.apache.ibatis.session.Configuration configuration = (org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration");
                Map<String, Object> newParameterMap = new HashMap<>(ids.length);
                parameterMappings = new ArrayList<>(ids.length);
                for (int i = 0; i < ids.length; i++) {
                    newParameterMap.put(idProperty + i, ids[i]);
                    parameterMappings.add(new ParameterMapping.Builder(configuration,
                            idProperty + i, Object.class).build());
                }
                parameterMap = newParameterMap;
            } else {
                parameterMap = Collections.emptyMap();
            }
            MyBatisUtils.getRealMetaObject(metaObject.getValue("delegate.parameterHandler"))
                    .setValue("delegate.parameterObject", parameterMap);
            metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
        }
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
