package cn.mybatisboost.sql;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;
import cn.mybatisboost.util.SqlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ConditionImpl implements Condition, Clause {

    private boolean mapUnderscoreToCamelCase;
    private NameAdaptor nameAdaptor;
    private List<ConditionClause> conditions = new ArrayList<>();
    private List<Object> parameters = new ArrayList<>();

    public ConditionImpl(boolean mapUnderscoreToCamelCase, NameAdaptor nameAdaptor) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
        this.nameAdaptor = nameAdaptor;
    }

    @Override
    public Condition and() {
        conditions.add(new SymbolConditionClause("AND"));
        return this;
    }

    @Override
    public Condition or() {
        conditions.add(new SymbolConditionClause("OR"));
        return this;
    }

    @Override
    public <A, B> Condition eq(Function<A, ?> column, B value) {
        LambdaUtils.LambdaInfo lambdaInfo = LambdaUtils.getLambdaInfo(column);
        conditions.add(new ParameterizedConditionClause("=",
                EntityUtils.getTableName(lambdaInfo.getType(), nameAdaptor) + "." + SqlUtils.normalizeColumn
                        (lambdaInfo.getMethodName().replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition eq(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition ne(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause("!=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition ne(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("!=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition lt(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause("<", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition lt(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("<", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition lte(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause("<=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition lte(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("<=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition gt(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause(">", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition gt(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause(">", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition gte(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause(">=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition gte(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause(">=", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition between(Function<A, ?> column, B minValue, B maxValue) {
        conditions.add(new ParameterizedConditionClause("BETWEEN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), minValue, maxValue));
        return this;
    }

    @Override
    public <A, B> Condition notBetween(Function<A, ?> column, B minValue, B maxValue) {
        conditions.add(new ParameterizedConditionClause("NOT BETWEEN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), minValue, maxValue));
        return this;
    }

    @Override
    public <A> Condition isNull(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("IS NULL", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A> Condition isNotNull(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("IS NOT NULL", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A> Condition isEmpty(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("= ''", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A> Condition isNotEmpty(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("!= ''", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition like(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause("LIKE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition like(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("LIKE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition notLike(Function<A, ?> column, B value) {
        conditions.add(new ParameterizedConditionClause("NOT LIKE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition notLike(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("NOT LIKE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition in(Function<A, ?> column, List<B> value) {
        conditions.add(new ParameterizedConditionClause("IN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition in(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("IN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A, B> Condition notIn(Function<A, ?> column, List<B> value) {
        conditions.add(new ParameterizedConditionClause("NOT IN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase), value));
        return this;
    }

    @Override
    public <A, B> Condition notIn(Function<A, ?> column, Function<B, ?> value) {
        conditions.add(new ColumnConditionClause("NOT IN", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase),
                SqlUtils.normalizeColumn(LambdaUtils.getLambdaInfo(value).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A> Condition isTrue(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("= TRUE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public <A> Condition isFalse(Function<A, ?> column) {
        conditions.add(new ParameterizedConditionClause("= FALSE", SqlUtils.normalizeColumn
                (LambdaUtils.getLambdaInfo(column).getMethodName()
                        .replaceFirst("^get", ""), mapUnderscoreToCamelCase)));
        return this;
    }

    @Override
    public void writeClause(StringBuilder sqlBuilder) {
        for (ConditionClause condition : conditions) {
            condition.writeClause(sqlBuilder);
            parameters.addAll(Arrays.asList(condition.getParameters()));
        }
    }
}
