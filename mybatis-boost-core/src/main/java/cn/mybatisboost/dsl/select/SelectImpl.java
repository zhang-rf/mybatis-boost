package cn.mybatisboost.dsl.select;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.dsl.condition.*;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;
import cn.mybatisboost.util.SqlUtils;

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

    public SelectImpl(boolean mapUnderscoreToCamelCase, NameAdaptor nameAdaptor) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
        this.nameAdaptor = nameAdaptor;
    }

    @Override
    public <T> Select select(Function<T, ?>... columns) {
        if (columns.length > 0) {
            List<String> columnList = new ArrayList<>();
            for (Function<T, ?> mr : columns) {
                LambdaUtils.LambdaInfo lambdaInfo = LambdaUtils.getLambdaInfo(mr);
                String table = EntityUtils.getTableName(lambdaInfo.getType(), nameAdaptor);
                String column = SqlUtils.normalizeColumn
                        (lambdaInfo.getMethodName().replaceFirst("^get", ""),
                                mapUnderscoreToCamelCase);
                columnList.add(table + "." + column);
            }
            this.columns = columnList.toArray(new String[0]);
        }
        return this;
    }

    @Override
    public Select from(Class<?>... tables) {
        this.tables = tables;
        return this;
    }

    @Override
    public Select where(Condition... condition) {
        conditions = condition;
        return this;
    }

    @Override
    public String sql() {
        StringBuilder sqlBuilder = new StringBuilder();
        appendSelect(sqlBuilder);
        appendFrom(sqlBuilder);
        appendWhere(sqlBuilder);
        return sqlBuilder.toString();
    }

    private void appendSelect(StringBuilder sqlBuilder) {
        sqlBuilder.append("SELECT ");
        if (columns.length == 0) {
            sqlBuilder.append("*");
        } else {
            for (String column : columns) {
                if (tables.length == 1) {
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

    private void appendWhere(StringBuilder sqlBuilder) {
        if (conditions.length > 0) {
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                writeCondition(sqlBuilder, condition, tables.length > 1);
                if (i < conditions.length - 1) {
                    if (condition instanceof ParameterizedColumnCondition || condition instanceof ConditionGroup) {
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

    @Override
    public Object[] parameters() {
        List<Object> parameters = new ArrayList<>();
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
}
