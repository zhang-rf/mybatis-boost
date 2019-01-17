package cn.mybatisboost.dsl.select;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.dsl.MappingUnderscoreToCamelCaseAware;
import cn.mybatisboost.dsl.NameAdaptorAware;
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
    private List<String> columns;
    private Class<?>[] tables;
    private Condition[] conditions;

    public SelectImpl(boolean mapUnderscoreToCamelCase, NameAdaptor nameAdaptor) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
        this.nameAdaptor = nameAdaptor;
    }

    @Override
    public <T> Select select(Function<T, ?>... columns) {
        this.columns = new ArrayList<>();
        for (Function<T, ?> mr : columns) {
            LambdaUtils.LambdaInfo lambdaInfo = LambdaUtils.getLambdaInfo(mr);
            String table = EntityUtils.getTableName(lambdaInfo.getType(), nameAdaptor);
            String column = SqlUtils.normalizeColumn
                    (lambdaInfo.getMethodName().replaceFirst("^get", ""), mapUnderscoreToCamelCase);
            this.columns.add(table + "." + column);
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
    public Object[] parameters() {
        List<Object> parameters = new ArrayList<>();
        for (Condition condition : conditions) {
            aggregateParameters(parameters, condition);
        }
        return parameters.toArray();
    }

    private void aggregateParameters(List<Object> parameters, Condition condition) {
        if (condition instanceof ColumnCondition) {
            parameters.addAll(Arrays.asList(((ColumnCondition) condition).getParameters()));
        } else if (condition instanceof ConditionGroup) {
            for (Condition subCondition : ((ConditionGroup) condition).getConditions()) {
                aggregateParameters(parameters, subCondition);
            }
        }
    }

    @Override
    public String sql() {
        StringBuilder sqlBuilder = new StringBuilder();
        writeSelect(sqlBuilder);
        writeFrom(sqlBuilder);
        writeWhere(sqlBuilder);
        return sqlBuilder.toString();
    }

    private void writeSelect(StringBuilder sqlBuilder) {
        sqlBuilder.append("SELECT ");
        if (columns.isEmpty()) {
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

    private void writeFrom(StringBuilder sqlBuilder) {
        sqlBuilder.append(" FROM ");
        for (Class<?> table : tables) {
            sqlBuilder.append(EntityUtils.getTableName(table, nameAdaptor)).append(", ");
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
    }

    private void writeWhere(StringBuilder sqlBuilder) {
        if (conditions.length > 0) {
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                writeCondition(sqlBuilder, condition, tables.length > 1);
                if (i < conditions.length - 1) {
                    if (condition instanceof ColumnCondition || condition instanceof ConditionGroup) {
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
        else if (condition instanceof ColumnCondition) {
            ColumnCondition columnCondition = (ColumnCondition) condition;
            if (columnCondition instanceof MappingUnderscoreToCamelCaseAware) {
                ((MappingUnderscoreToCamelCaseAware) columnCondition)
                        .setMappingUnderscoreToCamelCase(mapUnderscoreToCamelCase);
            }
            if (columnCondition instanceof NameAdaptorAware) {
                ((NameAdaptorAware) columnCondition).setNameAdaptor(nameAdaptor);
            }

            sqlBuilder.append(columnCondition.getColumn(withTableName)).append(" ").append(columnCondition.getSymbol());
            if (columnCondition.getParameters().length == 1) {
                sqlBuilder.append(" ? ");
            } else {
                for (int i = 0; i < columnCondition.getParameters().length; i++) {
                    sqlBuilder.append(" ? AND");
                }
                sqlBuilder.setLength(sqlBuilder.length() - 3);
            }
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
}
