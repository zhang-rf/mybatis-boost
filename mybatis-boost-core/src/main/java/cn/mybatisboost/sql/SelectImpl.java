package cn.mybatisboost.sql;

import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;

import java.util.*;

public class SelectImpl implements Select {

    private List<LambdaUtils.LambdaInfo> columns;
    private List<Class<?>> tables;
    private List<Condition> conditions = new ArrayList<>();
    private Queue<String> linkSymbols = new ArrayDeque<>();

    public SelectImpl(List<LambdaUtils.LambdaInfo> columns) {
        this.columns = columns;
    }

    @Override
    public Select from(Class<?>... tables) {
        this.tables = Arrays.asList(tables);
        return this;
    }

    @Override
    public Select where(Condition condition) {
        conditions.add(condition);
        return this;
    }

    @Override
    public Select groupBy() {
        return null;
    }

    @Override
    public Select having() {
        return null;
    }

    @Override
    public Select orderBy() {
        return null;
    }

    @Override
    public Select and() {
        linkSymbols.add("AND");
        return this;
    }

    @Override
    public Select or() {
        linkSymbols.add("OR");
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        if (columns.isEmpty()) {
            builder.append("* ");
        } else {
            for (LambdaUtils.LambdaInfo column : columns) {
                builder.append(String.format("'%s'.'%s', ",
                        EntityUtils.getTableName(column.getType(), new NoopNameAdaptor()),
                        column.getMethodName().replace("get", "")));
            }
            builder.setLength(builder.length() - 2);
        }
        builder.append(" FROM ");
        for (Class<?> table : tables) {
            builder.append(EntityUtils.getTableName(table, new NoopNameAdaptor())).append(", ");
        }
        builder.setLength(builder.length() - 2);
        if (!conditions.isEmpty()) {
            builder.append(" WHERE ");
            if (conditions.size() == 1) {
                ((Clause) conditions.get(0)).writeClause(builder);
            } else {
                for (Condition condition : conditions) {
                    builder.append('(');
                    ((Clause) condition).writeClause(builder);
                    builder.setLength(builder.length() - 1);
                    builder.append(") ");
                    if (!linkSymbols.isEmpty()) {
                        builder.append(linkSymbols.poll()).append(' ');
                    }
                }
                builder.setLength(builder.length() - 1);
            }
        }
        return builder.toString();
    }
}
