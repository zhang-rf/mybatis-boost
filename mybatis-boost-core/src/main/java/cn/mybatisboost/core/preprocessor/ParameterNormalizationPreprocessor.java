package cn.mybatisboost.core.preprocessor;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MapperUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.util.HashMap;
import java.util.Map;

public class ParameterNormalizationPreprocessor implements SqlProvider {

    @Override
    @SuppressWarnings("unchecked")
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject != null) {
            Class<?> entityType;
            try {
                entityType = MapperUtils.getEntityTypeFromMapper
                        (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            } catch (Exception ignored) {
                return;
            }
            if (parameterObject.getClass() != entityType) {
                if (parameterObject instanceof Map) {
                    Map<String, Object> parameterMap = (Map<String, Object>) parameterObject;
                    if (parameterMap.size() == 2 &&
                            parameterMap.containsKey("collection") && parameterMap.containsKey("list")) {
                        Object collection = parameterMap.get("collection");
                        parameterMap.clear();
                        parameterMap.put("arg0", collection);
                        parameterMap.put("param1", collection);
                    }
                } else {
                    Map<String, Object> parameterMap = new HashMap<>();
                    parameterMap.put("arg0", parameterObject);
                    parameterMap.put("param1", parameterObject);
                    metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
                    metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
                }
            }
        }
    }
}
