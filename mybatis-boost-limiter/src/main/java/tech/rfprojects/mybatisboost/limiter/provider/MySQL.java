package tech.rfprojects.mybatisboost.limiter.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.SqlUtils;

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
