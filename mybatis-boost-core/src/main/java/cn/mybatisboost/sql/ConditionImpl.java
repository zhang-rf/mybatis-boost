package cn.mybatisboost.sql;

import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConditionImpl implements Condition {

    private List<ConditionClause> conditions = new ArrayList<>();

    @Override
    public <T1, T2, R> Condition eq(Function<T1, R> column1, Function<T2, R> column2) {
        conditions.add(new ConditionClause("=", LambdaUtils.getLambdaInfo(column1).get(), LambdaUtils.getLambdaInfo(column2).get()));
        return this;
    }

    @Override
    public <T, R> Condition eq(Function<T, R> column, Object value) {
        conditions.add(new ConditionClause("=", LambdaUtils.getLambdaInfo(column).get(), value));
        return this;
    }

    @Override
    public Condition and() {
        conditions.add(new ConditionClause("AND"));
        return this;
    }

    @Override
    public Condition or() {
        conditions.add(new ConditionClause("OR"));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ConditionClause condition : conditions) {
            if (condition.getSymbol().equals("AND")) {
                builder.append("AND ");
            } else if (condition.getSymbol().equals("OR")) {
                builder.append("OR ");
            } else {
                LambdaUtils.LambdaInfo column = condition.getColumn1();
                builder.append(String.format("'%s'.'%s' ",
                        EntityUtils.getTableName(column.getType(), new NoopNameAdaptor()),
                        column.getMethodName().replace("get", "")));
                builder.append(condition.getSymbol()).append(' ');
                if (condition.getValue() != null) {
                    builder.append("? ");
                } else {
                    column = condition.getColumn2();
                    builder.append(String.format("'%s'.'%s' ",
                            EntityUtils.getTableName(column.getType(), new NoopNameAdaptor()),
                            column.getMethodName().replace("get", "")));
                }
            }
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private static class ConditionClause {

        private String symbol;
        private LambdaUtils.LambdaInfo column1;
        private LambdaUtils.LambdaInfo column2;
        private Object value;

        public ConditionClause(String symbol) {
            this.symbol = symbol;
        }

        public ConditionClause(String symbol, LambdaUtils.LambdaInfo column1, LambdaUtils.LambdaInfo column2) {
            this.symbol = symbol;
            this.column1 = column1;
            this.column2 = column2;
        }

        public ConditionClause(String symbol, LambdaUtils.LambdaInfo column, Object value) {
            this.symbol = symbol;
            this.column1 = column;
            this.value = value;
        }

        public String getSymbol() {
            return symbol;
        }

        public LambdaUtils.LambdaInfo getColumn1() {
            return column1;
        }

        public LambdaUtils.LambdaInfo getColumn2() {
            return column2;
        }

        public Object getValue() {
            return value;
        }
    }
}
