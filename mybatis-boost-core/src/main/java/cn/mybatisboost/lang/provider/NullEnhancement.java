package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Iterator;
import java.util.regex.Matcher;

public class NullEnhancement implements SqlProvider {

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
            String sql = boundSql.getSql();
            Matcher matcher = SqlUtils.PATTERN_PLACEHOLDER.matcher(sql);
            Iterator<ParameterMapping> iterator = boundSql.getParameterMappings().iterator();
            MetaObject parameterMetaObject = SystemMetaObject.forObject(boundSql.getParameterObject());
            StringBuilder sqlBuilder = new StringBuilder(sql);
            int offset = 0;
            while (matcher.find() && iterator.hasNext()) {
                try {
                    if (parameterMetaObject.getValue(iterator.next().getProperty()) != null) continue;
                } catch (Exception e) {
                    continue;
                }
                iterator.remove();
                String substring = sql.substring(matcher.start() - 3, matcher.end());
                substring = substring.replaceFirst("!= ?\\?$|<> ?\\$",
                        sql.startsWith("SELECT") ? "IS NOT NULL" : "is not null");
                if (substring.length() == 4) {
                    substring = substring.replaceFirst("= ?\\?$",
                            sql.startsWith("SELECT") ? "IS NULL" : "is null");
                }
                sqlBuilder.replace(matcher.start() + offset - 3, matcher.end() + offset, substring);
                offset += substring.length() - 4;
            }
            metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
        }
    }
}
