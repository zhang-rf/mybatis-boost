package cn.mybatisboost.id;

import java.util.UUID;

public class UuidGenerator implements IdGenerator<String> {

    @Override
    public String generateValue(Class<?> type, Class<?> fieldType) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
