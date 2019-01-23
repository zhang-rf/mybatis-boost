package cn.mybatisboost.dsl;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.dsl.select.Select;
import cn.mybatisboost.dsl.select.SelectImpl;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class Dsl {

    private static boolean mapUnderscoreToCamelCase;
    private static NameAdaptor nameAdaptor = new NoopNameAdaptor();

    public static void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        Dsl.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public static void setNameAdaptor(NameAdaptor nameAdaptor) {
        Dsl.nameAdaptor = nameAdaptor;
    }

    public static <T> Select select(Function<T, ?>... columns) {
        return new SelectImpl(mapUnderscoreToCamelCase, nameAdaptor).select(columns);
    }
}
