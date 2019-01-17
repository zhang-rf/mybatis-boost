package cn.mybatisboost.dsl;

public class SymbolConditionClause implements ConditionClause {

    private String symbol;

    public SymbolConditionClause(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void writeClause(StringBuilder sqlBuilder) {
        sqlBuilder.append(symbol).append(' ');
    }
}
