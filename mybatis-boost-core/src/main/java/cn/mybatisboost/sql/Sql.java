package cn.mybatisboost.sql;

import cn.mybatisboost.core.adaptor.NoopNameAdaptor;
import cn.mybatisboost.util.LambdaUtils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sql {

    public <T, R> Select select(Function<T, R>... columns) {
        return new SelectImpl(Arrays.stream(columns)
                .map(LambdaUtils::getLambdaInfo).collect(Collectors.toList()));
    }

    public static Condition newCondition() {
        return new ConditionImpl(true, new NoopNameAdaptor());
    }
}
