package cn.mybatisboost.generator;

import java.util.UUID;

public class UuidGenerator implements ValueGenerator<String> {

    @Override
    public String generateValue(Class<?> type, Class<?> fieldType) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
