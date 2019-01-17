package cn.mybatisboost.dsl.condition;

public class ConditionGroup implements Condition {

    private final Condition[] conditions;

    public ConditionGroup(Condition... conditions) {
        this.conditions = conditions;
    }

    public Condition[] getConditions() {
        return conditions;
    }
}
