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
        return new ParameterizedColumnCondition(column, "=", value);
    }

    public static <T1, T2> Condition eq(Function<T1, ?> column, Function<T2, ?> referencedColumn) {
        return new ReferencedColumnCondition(column, "=", referencedColumn);
    }

    public static <T, V> Condition ne(Function<T, ?> column, V value) {
        return new ParameterizedColumnCondition(column, "!=", value);
    }

    public static <T1, T2> Condition ne(Function<T1, ?> column, Function<T2, ?> referencedColumn) {
        return new ReferencedColumnCondition(column, "!=", referencedColumn);
    }
}
