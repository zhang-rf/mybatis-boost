package cn.mybatisboost.sql;

public class ParameterizedConditionClause implements ConditionClause {

    private String symbol;
    private String column;
    private Object[] parameters;

    public ParameterizedConditionClause(String symbol, String column, Object... parameters) {
        this.symbol = symbol;
        this.column = column;
        this.parameters = parameters;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public void writeClause(StringBuilder sqlBuilder) {
        sqlBuilder.append(column).append(' ').append(symbol);
        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                sqlBuilder.append(" ? AND");
            }
            sqlBuilder.setLength(sqlBuilder.length() - 4);
        }
        sqlBuilder.append(' ');
    }
}
