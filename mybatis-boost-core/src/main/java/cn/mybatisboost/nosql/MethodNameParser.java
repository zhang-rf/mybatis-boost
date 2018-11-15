package cn.mybatisboost.nosql;

import cn.mybatisboost.util.SqlUtils;
import cn.mybatisboost.util.tuple.BinaryTuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.*;

public class MethodNameParser {

    private final String methodName, tableName;
    private final boolean mapUnderscoreToCamelCase;
    private String parsedSql;
    private int offset, limit;

    public MethodNameParser(String methodName, String tableName, boolean mapUnderscoreToCamelCase) {
        this.methodName = methodName;
        this.tableName = tableName;
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public String toSql() {
        if (parsedSql != null) return parsedSql;
        StringBuilder sqlBuilder = new StringBuilder();
        Method method = Method.of(StringUtils.capitalize(methodName));
        sqlBuilder.append(method.sqlFragment()).append(' ').append(tableName).append(' ');

        String expression = this.methodName.substring(method.name().length());
        expression = prepare(sqlBuilder, expression);
        if (expression.isEmpty()) {
            return parsedSql = sqlBuilder.toString().trim();
        }

        Map<Integer, String> keywordMap = new TreeMap<>();
        for (String kw : Predicate.keywords()) {
            int index = -1;
            while ((index = expression.indexOf(kw, index + 1)) >= 0) {
                if (index + kw.length() == expression.length() ||
                        Character.isUpperCase(expression.charAt(index + kw.length()))) {
                    keywordMap.put(index, kw);
                }
            }
        }
        for (String it : keywordMap.values()) {
            expression = expression.replace(it, "?");
        }

        int offset = 0, predicateIndex;
        Iterator<String> iterator = keywordMap.values().iterator();
        Predicate predicate = null;
        while ((predicateIndex = expression.indexOf("?", offset)) >= 0) {
            predicate = Predicate.of(iterator.next());

            if (predicateIndex > offset) {
                sqlBuilder.append(SqlUtils.normalizeColumn
                        (expression.substring(offset, predicateIndex), mapUnderscoreToCamelCase)).append(' ');
                if (!predicate.containsParameters()) {
                    sqlBuilder.append("= ? ");
                }
            }
            sqlBuilder.append(predicate.sqlFragment()).append(' ');
            offset = predicateIndex + 1;
        }
        if (offset < expression.length() - 1) {
            sqlBuilder.append(SqlUtils.normalizeColumn
                    (expression.substring(offset), mapUnderscoreToCamelCase));
            if (predicate == null || predicate != Predicate.OrderBy) {
                sqlBuilder.append(" = ?");
            }
        }
        return parsedSql = sqlBuilder.toString().trim();
    }

    public RowBounds toRowBounds() {
        if (parsedSql == null) toSql();
        return offset > 0 || limit > 0 ? new RowBounds(offset, limit) : RowBounds.DEFAULT;
    }

    private String prepare(StringBuilder sqlBuilder, String expression) {
        if (expression.startsWith("All")) {
            expression = expression.substring(3);
            if (expression.startsWith("By")) {
                sqlBuilder.append("WHERE ");
                expression = expression.substring(2);
            }

            Optional<BinaryTuple<String, Integer>> optional =
                    extractKeyNumber(expression, "Limit", true);
            if (optional.isPresent()) {
                BinaryTuple<String, Integer> tuple = optional.get();
                limit = tuple.second();
                expression = tuple.first();

                optional = extractKeyNumber(expression, "Offset", true);
                if (optional.isPresent()) {
                    tuple = optional.get();
                    offset = tuple.second();
                    expression = tuple.first();
                }
            }
        } else {
            if (expression.startsWith("First")) {
                limit = 1;
                expression = expression.substring(5);
            } else if (expression.startsWith("Top")) {
                Optional<BinaryTuple<String, Integer>> optional =
                        extractKeyNumber(expression, "Top", false);
                if (optional.isPresent()) {
                    BinaryTuple<String, Integer> tuple = optional.get();
                    limit = tuple.second();
                    expression = tuple.first();
                }
            }
            if (expression.startsWith("By")) {
                sqlBuilder.append("WHERE ");
                expression = expression.substring(2);
            }
        }
        return expression;
    }

    private Optional<BinaryTuple<String, Integer>> extractKeyNumber(String s, String key, boolean fromEnd) {
        int index = fromEnd ? s.lastIndexOf(key) : s.indexOf(key);
        if (index < 0) return Optional.empty();

        char[] chars = s.toCharArray();
        for (int i = index + key.length(); i < chars.length; i++) {
            if (!Character.isDigit(chars[i]) || i + 1 == chars.length) {
                if (i + 1 == chars.length) i++;
                char[] copy = Arrays.copyOfRange(chars, index + key.length(), i);
                return Optional.of(new BinaryTuple<>
                        (new StringBuilder(s).replace(index, i, "").toString(),
                                Integer.parseInt(new String(copy))));
            }
        }
        throw new IllegalArgumentException("Invalid string");
    }
}
