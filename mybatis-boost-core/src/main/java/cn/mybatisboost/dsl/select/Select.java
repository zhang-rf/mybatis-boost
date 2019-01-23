package cn.mybatisboost.dsl.select;

import cn.mybatisboost.dsl.Statement;
import cn.mybatisboost.dsl.condition.Condition;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public interface Select extends Statement {

    <T> Select select(Function<T, ?>... columns);

    Select from(Class<?>... tables);

    Select where(Condition... conditions);

    Select join(Class<?>... tables);

    Select on(Condition... conditions);

    <T> Select orderBy(Function<T, ?>... columns);

    Select desc();

    Select offset(int offset);

    Select limit(int limit);
}
