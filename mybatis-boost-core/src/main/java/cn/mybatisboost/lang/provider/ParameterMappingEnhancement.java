package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class ParameterMappingEnhancement implements SqlProvider {

    @Override
    public void handle(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings.isEmpty() && parameterObject instanceof Map) {
            int parameterCount = SqlUtils.countPlaceholders(boundSql.getSql());
            if (parameterCount > 0) {
                Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
                Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
                for (int i = 1; parameterMap.containsKey("param" + i); i++) {
                    parameterMappings.add(new ParameterMapping.Builder
                            (configuration, "param" + i, Object.class).build());
                }
            }
        }
    }
}
