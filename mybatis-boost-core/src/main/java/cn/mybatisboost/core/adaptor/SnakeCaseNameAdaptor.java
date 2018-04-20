package cn.mybatisboost.core.adaptor;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SnakeCaseNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(name))
                .collect(Collectors.joining("_")).toLowerCase();
    }
}
