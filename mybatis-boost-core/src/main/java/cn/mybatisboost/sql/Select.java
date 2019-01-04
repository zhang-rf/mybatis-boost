package cn.mybatisboost.sql;

public interface Select {

    Select from(Class<?>... tables);

    Select where();

    Select groupBy();

    Select having();

    Select orderBy();
}
