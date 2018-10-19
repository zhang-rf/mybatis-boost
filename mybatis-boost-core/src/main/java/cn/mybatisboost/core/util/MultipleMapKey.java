package cn.mybatisboost.core.util;

import java.util.Arrays;
import java.util.Objects;

public class MultipleMapKey {

    private final Object[] keys;

    public MultipleMapKey(Object... keys) {
        this.keys = keys;
    }

    public Object[] getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipleMapKey multipleMapKey = (MultipleMapKey) o;
        if (keys.length != multipleMapKey.keys.length) return false;
        return Arrays.equals(keys, multipleMapKey.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }

    @Override
    public String toString() {
        return "MultipleMapKey" + Arrays.toString(keys);
    }
}
