package tech.rfprojects.mybatisboost.mapper;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.session.RowBounds;
import tech.rfprojects.mybatisboost.mapper.provider.*;

import java.util.List;

public interface GenericMapper<T> {

    @SelectProvider(type = Select.class, method = "reserved")
    T selectOne(T entity);

    @SelectProvider(type = SelectById.class, method = "reserved")
    T selectById(Object... id);

    @SelectProvider(type = Select.class, method = "reserved")
    List<T> select(T entity);

    @SelectProvider(type = Select.class, method = "reserved")
    List<T> selectWithRowBounds(T entity, RowBounds rowBounds);

    @SelectProvider(type = SelectAll.class, method = "reserved")
    List<T> selectAll();

    @SelectProvider(type = SelectAll.class, method = "reserved")
    List<T> selectAllWithRowBounds(RowBounds rowBounds);

    @InsertProvider(type = Insert.class, method = "reserved")
    int insert(T entity);

    @InsertProvider(type = Insert.class, method = "reserved")
    int insertSelective(T entity);

    @UpdateProvider(type = Update.class, method = "reserved")
    int update(T entity);

    @UpdateProvider(type = Update.class, method = "reserved")
    int updateSelective(T entity);

    @DeleteProvider(type = Delete.class, method = "reserved")
    int delete(T entity);
}
