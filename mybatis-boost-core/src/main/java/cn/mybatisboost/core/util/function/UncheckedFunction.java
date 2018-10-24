package cn.mybatisboost.core.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface UncheckedFunction<T, R> {

    R apply(T t) throws Throwable;

    static <T, R> Function<T, R> of(UncheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        };
    }
}
