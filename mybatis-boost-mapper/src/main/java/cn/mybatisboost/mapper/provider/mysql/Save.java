package cn.mybatisboost.mapper.provider.mysql;

import cn.mybatisboost.mapper.provider.Insert;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Save extends Insert {

    private static final Pattern PATTERN_COLUMNS =
            Pattern.compile("INSERT INTO \\w+ ?\\((.*?)\\)", Pattern.CASE_INSENSITIVE);

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        super.replace(metaObject, mappedStatement, boundSql);
        String sql = boundSql.getSql();
        Matcher matcher = PATTERN_COLUMNS.matcher(sql);
        if (matcher.find()) {
            StringBuilder builder = new StringBuilder(sql);
            builder.append(" ON DUPLICATE KEY UPDATE ");
            Arrays.stream(matcher.group(1).split(", "))
                    .forEach(it -> builder.append(it).append(" = VALUES(").append(it).append("), "));
            builder.setLength(builder.length() - 2);
            metaObject.setValue("delegate.boundSql.sql", builder.toString());
        }
    }
}
