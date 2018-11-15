package cn.mybatisboost.limiter.provider;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;

public class MySQL implements SqlProvider {

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        metaObject.setValue("delegate.boundSql.sql",
                SqlUtils.appendLimit(boundSql.getSql(), (RowBounds) metaObject.getValue("delegate.rowBounds")));
        metaObject.setValue("delegate.rowBounds", RowBounds.DEFAULT);
        metaObject.setValue("delegate.resultSetHandler.rowBounds", RowBounds.DEFAULT);
    }

    @Override
    public String toString() {
        return "MySQL";
    }
}
