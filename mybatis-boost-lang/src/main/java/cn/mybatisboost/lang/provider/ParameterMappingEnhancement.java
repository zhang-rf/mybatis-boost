package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterMappingEnhancement implements SqlProvider {

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings.isEmpty() && parameterObject instanceof Map) {
            Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
            parameterMappings = new ArrayList<>(parameterMap.size() / 2);
            Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
            for (int i = 1; parameterMap.containsKey("param" + i); i++) {
                parameterMappings.add(new ParameterMapping.Builder
                        (configuration, "param" + i, Object.class).build());
            }
            metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        }
    }
}
