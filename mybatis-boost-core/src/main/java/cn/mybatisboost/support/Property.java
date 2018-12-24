package cn.mybatisboost.support;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Property<T> {

    private final T value;

    private Property() {
        this.value = null;
    }

    private Property(T value) {
        this.value = value;
    }

    public static <T> Property<T> of(T value) {
        return new Property<>(value);
    }

    public T get() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Property)) {
            return false;
        }

        Property<?> other = (Property<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Property[%s]", value)
                : "Property.empty";
    }
}
