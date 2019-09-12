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
                throw sneakyThrow(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> E sneakyThrow(Throwable e) throws E {
        return (E) e;
    }
}
