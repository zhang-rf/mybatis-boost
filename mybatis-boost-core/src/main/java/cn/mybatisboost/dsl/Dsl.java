package cn.mybatisboost.dsl;

import cn.mybatisboost.core.adaptor.TPrefixedNameAdaptor;
import cn.mybatisboost.dsl.select.Select;
import cn.mybatisboost.dsl.select.SelectImpl;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class Dsl {

    public static <T> Select select(Function<T, ?>... columns) {
        return new SelectImpl(true, new TPrefixedNameAdaptor()).select(columns);
    }
}
