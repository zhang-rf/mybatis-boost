package cn.mybatisboost.mapper;

import cn.mybatisboost.mapper.provider.mysql.Replace;
import cn.mybatisboost.mapper.provider.mysql.Save;
import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

public interface MysqlCrudMapper<T> extends CrudMapper<T> {

    @InsertProvider(type = Save.class, method = "reserved")
    int save(T entity, String... properties);

    @InsertProvider(type = Save.class, method = "reserved")
    int saveSelective(T entity, String... properties);

    @InsertProvider(type = Save.class, method = "reserved")
    int batchSave(List<T> entity, String... properties);

    @InsertProvider(type = Save.class, method = "reserved")
    int batchSaveSelective(List<T> entity, String... properties);

    @InsertProvider(type = Replace.class, method = "reserved")
    int replace(T entity, String... properties);

    @InsertProvider(type = Replace.class, method = "reserved")
    int replaceSelective(T entity, String... properties);

    @InsertProvider(type = Replace.class, method = "reserved")
    int batchReplace(List<T> entity, String... properties);

    @InsertProvider(type = Replace.class, method = "reserved")
    int batchReplaceSelective(List<T> entity, String... properties);
}
