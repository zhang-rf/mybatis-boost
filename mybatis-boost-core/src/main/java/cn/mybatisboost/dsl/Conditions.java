package cn.mybatisboost.dsl;

import cn.mybatisboost.dsl.condition.*;

import java.util.function.Function;

public abstract class Conditions {

    public static Condition $(Condition... conditions) {
        return new ConditionGroup(conditions);
    }

    public static Condition or() {
        return new Or();
    }

    public static Condition not() {
        return new Not();
    }

    public static <T, V> Condition eq(Function<T, ?> column, V value) {
        return new SimpleColumnCondition(column, "=", value);
    }

    public static <T, V> Condition ne(Function<T, ?> column, V value) {
        return new SimpleColumnCondition(column, "!=", value);
    }
}
