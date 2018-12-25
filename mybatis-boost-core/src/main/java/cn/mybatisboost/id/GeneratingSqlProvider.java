package cn.mybatisboost.id;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.MapperUtils;
import cn.mybatisboost.util.MyBatisUtils;
import cn.mybatisboost.util.function.UncheckedFunction;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import javax.persistence.GeneratedValue;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GeneratingSqlProvider implements SqlProvider {

    private static ConcurrentMap<String, IdGenerator<?>> generatorCache = new ConcurrentHashMap<>();

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
            if (parameterObject.getClass() == type) {
                List<Field> generatedFields = EntityUtils.getGeneratedFields(type);
                if (!generatedFields.isEmpty()) {
                    try {
                        for (Field field : generatedFields) {
                            String generatorType = field.getAnnotation(GeneratedValue.class).generator();
                            IdGenerator<?> idGenerator = generatorCache.computeIfAbsent(generatorType,
                                    UncheckedFunction.of(key -> (IdGenerator<?>)
                                            GeneratingSqlProvider.class.getClassLoader().loadClass(key).newInstance()));

                            Object generateValue = idGenerator.generateValue(type, field.getType());
                            field.set(parameterObject, generateValue);
                            parameterObject = MyBatisUtils.getRealMetaObject
                                    (metaObject.getValue("delegate.parameterHandler"))
                                    .getValue("parameterObject");
                            field.set(parameterObject, generateValue);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
