package cn.mybatisboost.core.preprocessor;

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

public class AutoParameterMappingPreprocessor implements SqlProvider {

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings.isEmpty() && boundSql.getParameterObject() instanceof Map) {
            int parameterCount = SqlUtils.countPlaceholders(boundSql.getSql());
            if (parameterCount > 0) {
                Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
                for (int i = 1; i < parameterCount; i++) {
                    parameterMappings.add(new ParameterMapping.Builder
                            (configuration, "param" + i, Object.class).build());
                }
            }
        }
    }
}
