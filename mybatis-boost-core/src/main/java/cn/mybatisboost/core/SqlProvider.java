package cn.mybatisboost.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;

public interface SqlProvider {

    String MYBATIS_BOOST = "#MYBATIS_BOOST#";

    default String reserved() {
        return MYBATIS_BOOST;
    }

    void handle(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql);
}
