package cn.mybatisboost.mapper;

import cn.mybatisboost.mapper.provider.mysql.Save;
import org.apache.ibatis.annotations.InsertProvider;

public interface MysqlCrudMapper<T> extends CrudMapper<T> {

    @InsertProvider(type = Save.class, method = "reserved")
    int save(T entity, String... properties);

    @InsertProvider(type = Save.class, method = "reserved")
    int saveSelectively(T entity, String... properties);
}
