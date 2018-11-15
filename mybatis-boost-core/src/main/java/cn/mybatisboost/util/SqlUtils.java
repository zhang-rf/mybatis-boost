package cn.mybatisboost.util;

import cn.mybatisboost.util.tuple.BinaryTuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SqlUtils {

    public static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("(?<!')\\B\\?\\B(?!')");
    public static final Pattern PATTERN_COLUMN = Pattern.compile("(\\w+).*?(?<!')\\B\\?\\B(?!')");

    private static ConcurrentMap<String, Integer> placeholderCountCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, List<String>> columnsCache = new ConcurrentHashMap<>();

    public static StringBuilder appendWhere(StringBuilder sqlBuilder, Stream<String> stream) {
        sqlBuilder.append(" WHERE ");
        stream.forEach(c -> sqlBuilder.append(c).append(" = ? AND "));
        sqlBuilder.setLength(sqlBuilder.length() - 5);
        return sqlBuilder;
    }

    public static String appendLimit(String sql, RowBounds rowBounds) {
        if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET || rowBounds.getLimit() != RowBounds.NO_ROW_LIMIT) {
            sql += " LIMIT " + rowBounds.getOffset() + ", " + rowBounds.getLimit();
        }
        return sql;
    }

    public static String appendLimitOffset(String sql, RowBounds rowBounds) {
        if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET || rowBounds.getLimit() != RowBounds.NO_ROW_LIMIT) {
            sql += " LIMIT " + rowBounds.getLimit() + " OFFSET " + rowBounds.getOffset();
        }
        return sql;
    }

    public static int countPlaceholders(String sql) {
        return placeholderCountCache.computeIfAbsent(sql, k -> {
            int count = 0;
            Matcher matcher = PATTERN_PLACEHOLDER.matcher(sql);
            while (matcher.find()) count++;
            return count;
        });
    }

    public static List<String> findColumnsFromSQL(String sql) {
        return columnsCache.computeIfAbsent(sql, k -> {
            List<String> columns = new ArrayList<>();
            Matcher matcher = PATTERN_COLUMN.matcher(sql);
            while (matcher.find()) {
                columns.add(matcher.group(1));
            }
            return Collections.unmodifiableList(columns);
        });
    }

    public static String normalizeColumn(String column, boolean mapUnderscoreToCamelCase) {
        if (mapUnderscoreToCamelCase) {
            return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(column))
                    .map(StringUtils::uncapitalize).collect(Collectors.joining("_"));
        } else {
            return StringUtils.capitalize(column);
        }
    }

    public static BinaryTuple<List<String>, List<String>> getPropertiesAndColumnsFromLiteralColumns
            (String literalColumns, Class<?> entityType, boolean mapUnderscoreToCamelCase) {
        List<String> properties, columns;
        if (Objects.equals(literalColumns, "*") || literalColumns.toUpperCase().startsWith("NOT ")) {
            properties = EntityUtils.getProperties(entityType);
            if (literalColumns.toUpperCase().startsWith("NOT ")) {
                properties.removeAll
                        (Arrays.stream(literalColumns.substring(4).split(","))
                                .map(String::trim).map(PropertyUtils::normalizeProperty)
                                .collect(Collectors.toList()));
            }
            columns = properties.stream()
                    .map(it -> SqlUtils.normalizeColumn(it, mapUnderscoreToCamelCase)).collect(Collectors.toList());
        } else {
            columns = Arrays.stream(literalColumns.split(",")).map(String::trim)
                    .map(it -> SqlUtils.normalizeColumn(it, mapUnderscoreToCamelCase)).collect(Collectors.toList());
            properties = columns.stream().map(PropertyUtils::normalizeProperty).collect(Collectors.toList());
        }
        return new BinaryTuple<>(properties, columns);
    }
}
