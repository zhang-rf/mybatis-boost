package cn.mybatisboost.core.util.tuple;

import java.util.Objects;

public class BinaryTuple<T1, T2> {

    private T1 first;
    private T2 second;

    public BinaryTuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 first() {
        return first;
    }

    public T2 second() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryTuple<?, ?> that = (BinaryTuple<?, ?>) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "BinaryTuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
