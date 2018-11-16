package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Iterator;
import java.util.regex.Matcher;

public class NullEnhancement implements SqlProvider {
    private static Logger logger = LoggerFactory.getLogger(NullEnhancement.class);

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT ||
                mappedStatement.getSqlCommandType() == SqlCommandType.DELETE) {
            String sql = boundSql.getSql();

            Matcher matcher = SqlUtils.PATTERN_PLACEHOLDER.matcher(sql);
            Iterator<ParameterMapping> iterator = boundSql.getParameterMappings().iterator();
            MetaObject parameterMetaObject = SystemMetaObject.forObject(boundSql.getParameterObject());
            boolean isUpperCase = Character.isUpperCase(sql.charAt(0));

            int offset = 0;
            StringBuilder sqlBuilder = new StringBuilder();
            while (matcher.find() & iterator.hasNext()) {
                try {
                    if (parameterMetaObject.getValue(iterator.next().getProperty()) != null) continue;
                } catch (Exception ignored) {
                    continue;
                }
                iterator.remove();

                String substring = sql.substring(offset, matcher.end());
                logger.info("666666:" + substring);
                int before = substring.length();
                substring = substring.replaceFirst(" ?!= *\\?$| ?<> *\\?$",
                        isUpperCase ? " IS NOT NULL" : " is not null");
                if (substring.length() == before) {
                    substring = substring.replaceFirst(" ?= *\\?$", isUpperCase ? " IS NULL" : " is null");
                    logger.info("777777:" + substring);
                }
                sqlBuilder.append(substring);
                offset = matcher.end();
            }
            sqlBuilder.append(sql, offset, sql.length());
            metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
        }
    }
}
