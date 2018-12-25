package cn.mybatisboost.generator;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import cn.mybatisboost.util.function.UncheckedFunction;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import javax.persistence.GeneratedValue;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GeneratingSqlProvider implements SqlProvider {

    private static ConcurrentMap<String, ValueGenerator<?>> generatorCache = new ConcurrentHashMap<>();

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            Class<?> type;
            try {
                type = MapperUtils.getEntityTypeFromMapper
                        (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
            } catch (Exception ignored) {
                return;
            }
            Object parameterObject = boundSql.getParameterObject();
            List parameterList = null;
            if (parameterObject instanceof Map) {
                parameterObject = ((Map) parameterObject).get("param1");
                if (parameterObject instanceof List) {
                    parameterList = (List) parameterObject;
                }
            }
            if (parameterList == null) {
                parameterList = Collections.singletonList(parameterObject);
            } else if (parameterList.isEmpty() || parameterList.get(0).getClass() != type) return;

            List<Field> generatedFields = EntityUtils.getGeneratedFields(type);
            if (!generatedFields.isEmpty()) {
                try {
                    for (Object parameter : parameterList) {
                        for (Field field : generatedFields) {
                            String generatorType = field.getAnnotation(GeneratedValue.class).generator();
                            ValueGenerator<?> generator = generatorCache.computeIfAbsent(generatorType,
                                    UncheckedFunction.of(key -> (ValueGenerator<?>)
                                            GeneratingSqlProvider.class.getClassLoader().loadClass(key).newInstance()));
                            field.set(parameter, generator.generateValue(type, field.getType()));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
