package tech.rfprojects.mybatisboost.mapper;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.session.RowBounds;
import tech.rfprojects.mybatisboost.mapper.provider.*;

import java.util.List;

public interface GenericMapper<T> {

    @SelectProvider(type = Find.class, method = "reserved")
    T findOne(T entity);

    @SelectProvider(type = FindById.class, method = "reserved")
    T findById(Object... id);

    @SelectProvider(type = Find.class, method = "reserved")
    List<T> find(T entity);

    @SelectProvider(type = Find.class, method = "reserved")
    List<T> findWithRowBounds(T entity, RowBounds rowBounds);

    @SelectProvider(type = FindAll.class, method = "reserved")
    List<T> findAll();

    @SelectProvider(type = FindAll.class, method = "reserved")
    List<T> findAllWithRowBounds(RowBounds rowBounds);

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
