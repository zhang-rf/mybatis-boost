package cn.mybatisboost.dsl;

public class ColumnConditionClause implements ConditionClause {

    private String symbol;
    private String column1;
    private String column2;

    public ColumnConditionClause(String symbol, String column1, String column2) {
        this.symbol = symbol;
        this.column1 = column1;
        this.column2 = column2;
    }

    @Override
    public void writeClause(StringBuilder sqlBuilder) {
        sqlBuilder.append(column1).append(' ').append(symbol).append(' ').append(column2).append(' ');
    }
}
