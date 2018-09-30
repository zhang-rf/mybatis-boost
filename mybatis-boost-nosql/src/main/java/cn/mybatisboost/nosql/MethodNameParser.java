package cn.mybatisboost.nosql;

import cn.mybatisboost.core.util.EntityUtils;
import cn.mybatisboost.core.util.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.Objects;

public class MethodNameParser {

    private String declaringClassName;
    private String methodName;
    private boolean mapUnderscoreToCamelCase;
    private String sql;
    private int offset, limit;

    public MethodNameParser(String declaringClassName, String methodName, boolean mapUnderscoreToCamelCase) {
        this.declaringClassName = declaringClassName;
        this.methodName = methodName;
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public String toSql() {
        if (sql != null) return sql;
        Class<?> type = MapperUtils.getEntityTypeFromMapper(declaringClassName);
        String[] words = StringUtils.splitByCharacterTypeCamelCase(StringUtils.capitalize(methodName));
        StringBuilder sqlBuilder = new StringBuilder(), buffer = new StringBuilder();

        sqlBuilder.append(Command.valueOf(words[0]).sqlFragment()).append(" #t ");
        for (int i = 1; i < words.length; i++) {
            int step = process(sqlBuilder, words[i], i + 1 < words.length ? words[i + 1] : null);
            if (step < 0) {
                try {
                    Predicate predicate = Predicate.of(words[i]);
                    if (buffer.length() > 0) {
                        EntityUtils.getColumnFromProperty(type, buffer.toString(), mapUnderscoreToCamelCase)
                                .ifPresent(it -> sqlBuilder.append(it).append(' '));
                        if (!predicate.conditional()) {
                            sqlBuilder.append("= ? ");
                        }
                        buffer.setLength(0);
                        i--;
                    } else {
                        sqlBuilder.append(predicate.sqlFragment()).append(' ');
                    }
                } catch (IllegalArgumentException ignored) {
                    buffer.append(words[i]);
                }
            } else {
                i += step;
            }
        }
        if (buffer.length() > 0) {
            EntityUtils.getColumnFromProperty(type, buffer.toString(), mapUnderscoreToCamelCase)
                    .ifPresent(it -> sqlBuilder.append(it).append(" = ?"));
        }
        return sql = sqlBuilder.toString().trim();
    }

    public RowBounds toRowBounds() {
        if (sql == null) toSql();
        return offset > 0 || limit > 0 ? new RowBounds(offset, limit) : RowBounds.DEFAULT;
    }

    private int process(StringBuilder sqlBuilder, String word, String next) {
        if (Objects.equals("All", word)) return 0;
        if (Objects.equals("By", word)) {
            sqlBuilder.append("WHERE ");
            return 0;
        }
        if (Objects.equals("First", word)) {
            limit = 1;
            return 0;
        }
        if (word.startsWith("Top")) {
            limit = Integer.parseInt(next);
            return 1;
        }
        if (word.startsWith("Offset")) {
            offset = Integer.parseInt(next);
            return 1;
        }
        if (word.startsWith("Limit")) {
            limit = Integer.parseInt(next);
            return 1;
        }
        return -1;
    }
}
