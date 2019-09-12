package cn.mybatisboost.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface UncheckedFunction<T, R> {

    R apply(T t) throws Throwable;

    static <T, R> Function<T, R> of(UncheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw sneakyThrow(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> E sneakyThrow(Throwable e) throws E {
        return (E) e;
    }
}
