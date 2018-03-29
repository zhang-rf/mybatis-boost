package tech.rfprojects.mybatisboost.core.adaptor;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LowercaseUnderscoreNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(name))
                .map(String::toLowerCase)
                .collect(Collectors.joining("_"));
    }
}
