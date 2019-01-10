package cn.mybatisboost.sql;

import java.util.List;
import java.util.function.Function;

public interface Condition {

    Condition and();

    Condition or();

    <A, B> Condition eq(Function<A, ?> column, B value);

    <A, B> Condition eq(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition ne(Function<A, ?> column, B value);

    <A, B> Condition ne(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition lt(Function<A, ?> column, B value);

    <A, B> Condition lt(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition lte(Function<A, ?> column, B value);

    <A, B> Condition lte(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition gt(Function<A, ?> column, B value);

    <A, B> Condition gt(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition gte(Function<A, ?> column, B value);

    <A, B> Condition gte(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition between(Function<A, ?> column, B minValue, B maxValue);

    <A, B> Condition notBetween(Function<A, ?> column, B minValue, B maxValue);

    <A> Condition isNull(Function<A, ?> column);

    <A> Condition isNotNull(Function<A, ?> column);

    <A> Condition isEmpty(Function<A, ?> column);

    <A> Condition isNotEmpty(Function<A, ?> column);

    <A, B> Condition like(Function<A, ?> column, B value);

    <A, B> Condition like(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition notLike(Function<A, ?> column, B value);

    <A, B> Condition notLike(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition in(Function<A, ?> column, List<B> value);

    <A, B> Condition in(Function<A, ?> column, Function<B, ?> value);

    <A, B> Condition notIn(Function<A, ?> column, List<B> value);

    <A, B> Condition notIn(Function<A, ?> column, Function<B, ?> value);

    <A> Condition isTrue(Function<A, ?> column);

    <A> Condition isFalse(Function<A, ?> column);
}
