package cn.mybatisboost.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;

public interface SqlProvider {

    String MYBATIS_BOOST = "#mybatisboost#";

    default String reserved() {
        return MYBATIS_BOOST;
    }

    void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql);
}
