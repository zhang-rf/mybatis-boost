package cn.mybatisboost.core.adaptor;

import org.apache.commons.lang3.StringUtils;

public class SnakeCaseNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return String.join("_", StringUtils.splitByCharacterTypeCamelCase(name)).toLowerCase();
    }
}
