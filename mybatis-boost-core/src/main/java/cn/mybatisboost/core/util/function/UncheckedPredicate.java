package cn.mybatisboost.core.util.function;

import java.util.function.Predicate;

@FunctionalInterface
public interface UncheckedPredicate<T> {

    boolean test(T t) throws Exception;

    static <T> Predicate<T> of(UncheckedPredicate<T> predicate) {
        return (t) -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
