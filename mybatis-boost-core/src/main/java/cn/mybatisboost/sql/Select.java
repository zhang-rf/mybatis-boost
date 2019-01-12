package cn.mybatisboost.sql;

public interface Select {

    Select from(Class<?>... tables);

    Select where(Condition condition);

    Select groupBy();

    Select having();

    Select orderBy();

    Select and();

    Select or();
}
