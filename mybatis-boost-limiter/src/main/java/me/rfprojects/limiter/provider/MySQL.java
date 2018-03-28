package me.rfprojects.limiter.provider;

import me.rfprojects.core.SqlProvider;
import me.rfprojects.core.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

public class MySQL implements SqlProvider {

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
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
