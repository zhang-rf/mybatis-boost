package cn.mybatisboost.core.preprocessor;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MapperUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.util.HashMap;
import java.util.Map;

public class SingleParameterPreprocessor implements SqlProvider {

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject.getClass() != entityType && !(parameterObject instanceof Map)) {
            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("arg0", parameterObject);
            parameterMap.put("param1", parameterObject);
            metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
            metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
        }
    }
}
