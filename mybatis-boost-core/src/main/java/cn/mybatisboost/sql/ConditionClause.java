package cn.mybatisboost.sql;

public interface ConditionClause extends Clause {

    default Object[] getParameters() {
        return new Object[0];
    }
}
