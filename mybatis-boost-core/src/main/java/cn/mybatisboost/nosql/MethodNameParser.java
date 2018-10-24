package cn.mybatisboost.nosql;

import cn.mybatisboost.core.util.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.Objects;

public class MethodNameParser {

    private String methodName, tableName;
    private boolean mapUnderscoreToCamelCase;
    private String parsedSql;
    private int offset, limit;

    public MethodNameParser(String methodName, String tableName, boolean mapUnderscoreToCamelCase) {
        this.methodName = methodName;
        this.tableName = tableName;
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public String toSql() {
        if (parsedSql != null) return parsedSql;

        String[] words = StringUtils.splitByCharacterTypeCamelCase(StringUtils.capitalize(methodName));
        StringBuilder sqlBuilder = new StringBuilder(), buffer = new StringBuilder();
        sqlBuilder.append(Command.valueOf(words[0]).sqlFragment()).append(' ').append(tableName).append(' ');
        for (int i = 1; i < words.length; i++) {
            int step = process(sqlBuilder, words[i], i + 1 < words.length ? words[i + 1] : null);
            if (step >= 0) {
                i += step;
            } else {
                try {
                    Predicate predicate = Predicate.of(words[i]);
                    if (buffer.length() > 0) {
                        sqlBuilder.append(SqlUtils.normalizeColumn(buffer.toString(), mapUnderscoreToCamelCase))
                                .append(' ');
                        buffer.setLength(0);
                        if (!predicate.conditional()) sqlBuilder.append("= ? ");
                    }
                    sqlBuilder.append(predicate.sqlFragment()).append(' ');
                } catch (IllegalArgumentException ignored) {
                    buffer.append(words[i]);
                    try {
                        Predicate predicate = Predicate.of(buffer.toString());
                        buffer.setLength(0);
                        sqlBuilder.append(predicate.sqlFragment()).append(' ');
                    } catch (IllegalArgumentException ignored2) {
                    }
                }
            }
        }
        if (buffer.length() > 0) {
            sqlBuilder.append(SqlUtils.normalizeColumn(buffer.toString(), mapUnderscoreToCamelCase)).append(" = ?");
        }
        return parsedSql = sqlBuilder.toString().trim();
    }

    public RowBounds toRowBounds() {
        if (parsedSql == null) toSql();
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
