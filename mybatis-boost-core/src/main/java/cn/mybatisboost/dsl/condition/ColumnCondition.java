package cn.mybatisboost.dsl.condition;

public interface ColumnCondition extends Condition {

    String getColumn(boolean withTableName);

    String getSymbol();

    Object[] getParameters();
}
