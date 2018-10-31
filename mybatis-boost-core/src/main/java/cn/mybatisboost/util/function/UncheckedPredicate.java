package cn.mybatisboost.util.function;

import java.util.function.Predicate;

@FunctionalInterface
public interface UncheckedPredicate<T> {

    boolean test(T t) throws Throwable;

    static <T> Predicate<T> of(UncheckedPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        };
    }
}
