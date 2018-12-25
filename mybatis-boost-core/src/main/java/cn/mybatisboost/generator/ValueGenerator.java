package cn.mybatisboost.generator;

public interface ValueGenerator<T> {

    T generateValue(Class<?> type, Class<?> fieldType);
}
