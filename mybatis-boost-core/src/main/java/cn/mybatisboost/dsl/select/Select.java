package cn.mybatisboost.dsl.select;

import cn.mybatisboost.dsl.Statement;
import cn.mybatisboost.dsl.condition.Condition;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public interface Select extends Statement {

    <T> Select select(Function<T, ?>... columns);

    Select from(Class<?>... tables);

    Select where(Condition... condition);
}
