package cn.mybatisboost.dsl.select;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.dsl.condition.*;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;
import cn.mybatisboost.util.SqlUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SelectImpl implements Select {

    private boolean mapUnderscoreToCamelCase;
    private NameAdaptor nameAdaptor;
    private String[] columns = new String[0];
    private Class<?>[] tables;
    private Condition[] conditions = new Condition[0];
    private Class<?>[] joiningTables = new Class[0];
    private Condition[] joiningConditions = new Condition[0];
    private String[] orderColumns = new String[0];
    private boolean desc;
    private int offset, limit;

    public SelectImpl(boolean mapUnderscoreToCamelCase, NameAdaptor nameAdaptor) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
        this.nameAdaptor = nameAdaptor;
    }

    @Override
    public <T> Select select(Function<T, ?>... columns) {
        if (columns.length > 0) {
            this.columns = convertToLiteralColumns(columns);
        }
        return this;
    }

    @Override
    public Select from(Class<?>... tables) {
        this.tables = tables;
        return this;
    }

    @Override
    public Select where(Condition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public Select join(Class<?>... tables) {
        joiningTables = tables;
        return this;
    }

    @Override
    public Select on(Condition... conditions) {
        joiningConditions = conditions;
        return this;
    }

    @Override
    public <T> Select orderBy(Function<T, ?>... columns) {
        if (columns.length > 0) {
            orderColumns = convertToLiteralColumns(columns);
        }
        return this;
    }

    @Override
    public Select desc() {
        desc = true;
        return this;
    }

    @Override
    public Select offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Select limit(int limit) {
        this.limit = limit;
        return this;
    }

    private <T> String[] convertToLiteralColumns(Function<T, ?>[] columns) {
        List<String> columnList = new ArrayList<>();
        for (Function<T, ?> mr : columns) {
            LambdaUtils.LambdaInfo lambdaInfo = LambdaUtils.getLambdaInfo(mr);
            String table = EntityUtils.getTableName(lambdaInfo.getType(), nameAdaptor);
            String column = SqlUtils.normalizeColumn
                    (lambdaInfo.getMethodName().replaceFirst("^get", ""),
                            mapUnderscoreToCamelCase);
            columnList.add(table + "." + column);
        }
        return columnList.toArray(new String[0]);
    }

    @Override
    public String sql() {
        StringBuilder sqlBuilder = new StringBuilder();
        appendSelect(sqlBuilder);
        appendFrom(sqlBuilder);
        if (joiningTables.length > 0) {
            appendJoin(sqlBuilder);
            appendOn(sqlBuilder);
        }
        appendWhere(sqlBuilder);
        if (orderColumns.length > 0) {
            appendOrderBy(sqlBuilder, desc);
        }
        return sqlBuilder.toString();
    }

    private void appendSelect(StringBuilder sqlBuilder) {
        sqlBuilder.append("SELECT ");
        if (columns.length == 0) {
            sqlBuilder.append("*");
        } else {
            for (String column : columns) {
                if (tables.length == 1 && joiningTables.length == 0) {
                    sqlBuilder.append(column.split("\\.")[1]);
                } else {
                    sqlBuilder.append(column);
                }
                sqlBuilder.append(", ");
            }
            sqlBuilder.setLength(sqlBuilder.length() - 2);
        }
    }

    private void appendFrom(StringBuilder sqlBuilder) {
        sqlBuilder.append(" FROM ");
        for (Class<?> table : tables) {
            sqlBuilder.append(EntityUtils.getTableName(table, nameAdaptor)).append(", ");
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
    }

    private void appendJoin(StringBuilder sqlBuilder) {
        sqlBuilder.append(" JOIN ");
        for (Class<?> table : joiningTables) {
            sqlBuilder.append(EntityUtils.getTableName(table, nameAdaptor)).append(", ");
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
    }

    private void appendOn(StringBuilder sqlBuilder) {
        sqlBuilder.append(" ON ");
        for (int i = 0; i < joiningConditions.length; i++) {
            Condition condition = joiningConditions[i];
            writeCondition(sqlBuilder, condition, true);
            if (i < joiningConditions.length - 1) {
                if (condition instanceof ParameterizedColumnCondition ||
                        condition instanceof ReferencedColumnCondition ||
                        condition instanceof ConditionGroup) {
                    if (!(joiningConditions[i + 1] instanceof Or)) sqlBuilder.append("AND ");
                }
            }
        }
        sqlBuilder.setLength(sqlBuilder.length() - 1);
    }

    private void appendWhere(StringBuilder sqlBuilder) {
        if (conditions.length > 0) {
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                writeCondition(sqlBuilder, condition, tables.length > 1 || joiningTables.length > 0);
                if (i < conditions.length - 1) {
                    if (condition instanceof ParameterizedColumnCondition ||
                            condition instanceof ReferencedColumnCondition ||
                            condition instanceof ConditionGroup) {
                        if (!(conditions[i + 1] instanceof Or)) sqlBuilder.append("AND ");
                    }
                }
            }
            sqlBuilder.setLength(sqlBuilder.length() - 1);
        }
    }

    private void writeCondition(StringBuilder sqlBuilder, Condition condition, boolean withTableName) {
        if (condition instanceof Not) sqlBuilder.append("NOT ");
        else if (condition instanceof Or) sqlBuilder.append("OR ");
        else if (condition instanceof ParameterizedColumnCondition) {
            ParameterizedColumnCondition columnCondition = (ParameterizedColumnCondition) condition;
            columnCondition.setMappingUnderscoreToCamelCase(mapUnderscoreToCamelCase);
            columnCondition.setNameAdaptor(nameAdaptor);

            sqlBuilder.append(columnCondition.getColumn(withTableName)).append(" ").append(columnCondition.getSymbol());
            if (columnCondition.getParameters().length == 1) {
                sqlBuilder.append(" ? ");
            } else {
                for (int i = 0; i < columnCondition.getParameters().length; i++) {
                    sqlBuilder.append(" ? AND");
                }
                sqlBuilder.setLength(sqlBuilder.length() - 3);
            }
        } else if (condition instanceof ReferencedColumnCondition) {
            ReferencedColumnCondition columnCondition = (ReferencedColumnCondition) condition;
            columnCondition.setMappingUnderscoreToCamelCase(mapUnderscoreToCamelCase);
            columnCondition.setNameAdaptor(nameAdaptor);
            sqlBuilder.append(columnCondition.getColumn()).append(" ").append(columnCondition.getSymbol()).append(" ")
                    .append(columnCondition.getReferencedColumn()).append(" ");
        } else if (condition instanceof ConditionGroup) {
            ConditionGroup conditionGroup = (ConditionGroup) condition;
            sqlBuilder.append("(");
            for (Condition c : conditionGroup.getConditions()) {
                writeCondition(sqlBuilder, c, withTableName);
            }
            sqlBuilder.setLength(sqlBuilder.length() - 1);
            sqlBuilder.append(") ");
        }
    }

    private void appendOrderBy(StringBuilder sqlBuilder, boolean desc) {
        sqlBuilder.append(" ORDER BY ");
        for (String column : orderColumns) {
            if (tables.length == 1 && joiningTables.length == 0) {
                sqlBuilder.append(column.split("\\.")[1]);
            } else {
                sqlBuilder.append(column);
            }
            sqlBuilder.append(", ");
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
        if (desc) {
            sqlBuilder.append(" DESC");
        }
    }

    @Override
    public Object[] parameters() {
        List<Object> parameters = new ArrayList<>();
        for (Condition condition : joiningConditions) {
            aggregateParameters(parameters, condition);
        }
        for (Condition condition : conditions) {
            aggregateParameters(parameters, condition);
        }
        return parameters.toArray();
    }

    private void aggregateParameters(List<Object> parameters, Condition condition) {
        if (condition instanceof ParameterizedColumnCondition) {
            parameters.addAll(Arrays.asList(((ParameterizedColumnCondition) condition).getParameters()));
        } else if (condition instanceof ConditionGroup) {
            for (Condition subCondition : ((ConditionGroup) condition).getConditions()) {
                aggregateParameters(parameters, subCondition);
            }
        }
    }

    @Override
    public RowBounds rowBounds() {
        return offset != 0 || limit != 0 ? new RowBounds(offset, limit) : RowBounds.DEFAULT;
    }
}
