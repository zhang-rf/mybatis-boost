package tech.rfprojects.mybatisboost.core.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface UncheckedConsumer<T> {

    void accept(T t) throws Exception;

    static <T> Consumer<T> of(UncheckedConsumer<T> consumer) {
        return (t) -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
