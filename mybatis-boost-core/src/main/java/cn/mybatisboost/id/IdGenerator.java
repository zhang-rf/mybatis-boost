package cn.mybatisboost.id;

public interface IdGenerator<T> {

    T generateValue(Class<?> type, Class<?> fieldType);
}
