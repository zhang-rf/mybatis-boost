package cn.mybatisboost.mapper;

import cn.mybatisboost.core.GenericMapper;
import cn.mybatisboost.mapper.provider.Delete;
import cn.mybatisboost.mapper.provider.*;
import cn.mybatisboost.mapper.provider.Insert;
import cn.mybatisboost.mapper.provider.Update;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface CrudMapper<T> extends GenericMapper<T> {

    @SelectProvider(type = SelectOrCount.class, method = "reserved")
    int count(T entity, String... conditionProperties);

    @SelectProvider(type = SelectOrCount.class, method = "reserved")
    T selectOne(T entity, String... conditionProperties);

    @SelectProvider(type = SelectOrCount.class, method = "reserved")
    List<T> select(T entity, String... conditionProperties);

    @SelectProvider(type = SelectOrCount.class, method = "reserved")
    List<T> selectWithRowBounds(T entity, RowBounds rowBounds, String... conditionProperties);

    @SelectProvider(type = SelectOrCountAll.class, method = "reserved")
    int countAll();

    @SelectProvider(type = SelectOrCountAll.class, method = "reserved")
    List<T> selectAll();

    @SelectProvider(type = SelectOrCountAll.class, method = "reserved")
    List<T> selectAllWithRowBounds(RowBounds rowBounds);

    @SelectProvider(type = SelectByIds.class, method = "reserved")
    T selectById(@Param("arg0") Object id);

    @SelectProvider(type = SelectByIds.class, method = "reserved")
    List<T> selectByIds(Object... ids);

    @InsertProvider(type = Insert.class, method = "reserved")
    int insert(T entity, String... properties);

    @InsertProvider(type = Insert.class, method = "reserved")
    int batchInsert(List<T> entities, String... properties);

    @InsertProvider(type = Insert.class, method = "reserved")
    int insertSelective(T entity, String... properties);

    @InsertProvider(type = Insert.class, method = "reserved")
    int batchInsertSelective(List<T> entities, String... properties);

    @UpdateProvider(type = Update.class, method = "reserved")
    int update(T entity, String... conditionProperties);

    @UpdateProvider(type = Update.class, method = "reserved")
    int updatePartial(T entity, String[] properties, String... conditionProperties);

    @UpdateProvider(type = Update.class, method = "reserved")
    int updateSelective(T entity, String... conditionProperties);

    @UpdateProvider(type = Update.class, method = "reserved")
    int updatePartialSelective(T entity, String[] properties, String... conditionProperties);

    @DeleteProvider(type = Delete.class, method = "reserved")
    int delete(T entity, String... conditionProperties);

    @DeleteProvider(type = DeleteByIds.class, method = "reserved")
    int deleteByIds(Object... ids);
}
