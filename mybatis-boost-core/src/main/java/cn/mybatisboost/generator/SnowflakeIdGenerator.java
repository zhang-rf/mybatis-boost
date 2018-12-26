package cn.mybatisboost.generator;

public class SnowflakeIdGenerator implements ValueGenerator<Long> {

    private Snowflake snowflake = new Snowflake(1545825894992L, 0);

    @Override
    public Long generateValue(Class<?> type, Class<?> fieldType) {
        try {
            return snowflake.next();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
