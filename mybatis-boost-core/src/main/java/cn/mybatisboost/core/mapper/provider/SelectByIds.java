package cn.mybatisboost.core.mapper.provider;

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
        List<ParameterMapping> parameterMappings = Collections.emptyList();
        boolean multipleIds = mappedStatement.getId().endsWith("Ids");
        if (!multipleIds) {
            sqlBuilder.append(" WHERE ").append(idProperty).append(" = ?");
            parameterMappings = Collections.singletonList(new ParameterMapping.Builder
                    ((org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration"),
                            "arg0", Object.class).build());
        } else {
            Map parameterMap = (Map) boundSql.getParameterObject();
            Object[] parameterArray = (Object[]) parameterMap.get("array");
            if (parameterArray.length > 0) {
                sqlBuilder.append(" WHERE ").append(idProperty).append(" IN (");
                Arrays.stream(parameterArray).forEach(c -> sqlBuilder.append("?, "));
                sqlBuilder.setLength(sqlBuilder.length() - 2);
                sqlBuilder.append(')');

                Map<String, Object> newParameterMap = new HashMap<>(parameterArray.length);
                parameterMappings = new ArrayList<>(parameterArray.length);
                for (int i = 0; i < parameterArray.length; i++) {
                    newParameterMap.put(idProperty + i, parameterArray[i]);
                    parameterMappings.add(new ParameterMapping.Builder((org.apache.ibatis.session.Configuration)
                            metaObject.getValue("delegate.configuration"),
                            idProperty + i, Object.class).build());
                }
                parameterMap = newParameterMap;
            } else {
                parameterMap = Collections.emptyMap();
            }
            metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
            metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
        }
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
