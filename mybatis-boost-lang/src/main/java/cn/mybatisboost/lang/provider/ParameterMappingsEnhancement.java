package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.List;

public class ParameterMappingsEnhancement implements SqlProvider {

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings.isEmpty()) {
            int parameterCount = StringUtils.countMatches(boundSql.getSql(), '?');
            Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
            for (int i = 0; i < parameterCount; i++) {
                parameterMappings.add(new ParameterMapping.Builder
                        (configuration, "arg" + i, Object.class).build());
            }
        }
    }
}
