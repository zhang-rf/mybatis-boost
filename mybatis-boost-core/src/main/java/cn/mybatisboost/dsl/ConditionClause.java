package cn.mybatisboost.dsl;

public interface ConditionClause extends Clause {

    default Object[] getParameters() {
        return new Object[0];
    }
}
