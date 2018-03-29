package tech.rfprojects.mybatisboost.core.adaptor;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnderscoreNameAdaptor implements NameAdaptor {

    private final boolean uppercase;

    public UnderscoreNameAdaptor(boolean uppercase) {
        this.uppercase = uppercase;
    }

    @Override
    public String adapt(String name) {
        Stream<String> stream = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(name));
        if (uppercase) {
            stream = stream.map(String::toUpperCase);
        } else {
            stream = stream.map(String::toLowerCase);
        }
        return stream.collect(Collectors.joining("_"));
    }
}
