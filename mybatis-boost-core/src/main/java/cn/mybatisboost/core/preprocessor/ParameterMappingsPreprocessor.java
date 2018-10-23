package cn.mybatisboost.core.preprocessor;

import cn.mybatisboost.core.SqlProvider;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.ArrayList;

public class ParameterMappingsPreprocessor implements SqlProvider {

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        metaObject.setValue("delegate.boundSql.parameterMappings",
                new ArrayList<>(boundSql.getParameterMappings()));
    }
}
