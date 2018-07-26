package cn.mybatisboost.mapper.provider;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.*;

public class SelectByIds implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        String idProperty = EntityUtils.getIdProperty(entityType);
        boolean multipleIds = mappedStatement.getId().endsWith("Ids");
        List<ParameterMapping> parameterMappings = Collections.emptyList();
        if (!multipleIds) {
            sqlBuilder.append(" WHERE ").append(idProperty).append(" = ?");
            parameterMappings = Collections.singletonList(new ParameterMapping.Builder
                    ((org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration"),
                            "param1", Object.class).build());
        } else {
            Map<?, ?> parameterMap = (Map<?, ?>) boundSql.getParameterObject();
            Object[] ids = (Object[]) parameterMap.get("param1");
            if (ids.length > 0) {
                sqlBuilder.append(" WHERE ").append(idProperty).append(" IN (");
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
            metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
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
