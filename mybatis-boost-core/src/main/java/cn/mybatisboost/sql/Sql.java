package cn.mybatisboost.sql;

import cn.mybatisboost.util.LambdaUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sql {

    public <T, R> Select select(Function<T, R>... columns) {
        return new SimpleSelect(Arrays.stream(columns)
                .map(LambdaUtils::getLambdaInfo).map(Optional::get).collect(Collectors.toList()));
    }
}
