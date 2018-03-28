package me.rfprojects.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

public interface SqlProvider {

    String MYBATIS_BOOST = "#mybatis-boost#";

    default String reserved() {
        return MYBATIS_BOOST;
    }

    void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql);
}
