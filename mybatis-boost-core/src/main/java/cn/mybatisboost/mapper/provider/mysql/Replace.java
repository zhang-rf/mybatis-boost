package cn.mybatisboost.mapper.provider.mysql;

import cn.mybatisboost.mapper.provider.Insert;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;

public class Replace extends Insert {

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        super.replace(connection, metaObject, mappedStatement, boundSql);
        String sql = boundSql.getSql();
        metaObject.setValue("delegate.boundSql.sql", sql.startsWith("INSERT") ?
                sql.replaceFirst("INSERT", "REPLACE") :
                sql.replaceFirst("insert", "replace"));
    }
}
