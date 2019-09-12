package cn.mybatisboost.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface UncheckedConsumer<T> {

    void accept(T t) throws Throwable;

    static <T> Consumer<T> of(UncheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
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
