package cn.mybatisboost.test;

import cn.mybatisboost.core.mapper.CrudMapper;
import org.apache.ibatis.annotations.*;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProjectMapper extends CrudMapper<Project> {

    @Select("select * from #t")
    List<Project> selectFromT();

    @Select("select * from project where id in #{ids}")
    List<Project> selectRange(@Param("ids") Collection<Integer> ids);

    @Select("select * from #t where id = ?")
    Project selectOneFromT(Integer id);

    @Insert("insert *")
    @Options(useGeneratedKeys = true)
    int insertSome(Collection<Project> collection);

    @Insert("insert group_id")
    @Options(useGeneratedKeys = true)
    int insertOne1(Project project);

    @Insert("insert NOT artifact_id")
    @Options(useGeneratedKeys = true)
    int insertOne2(Project project);

    @Update("update set group_id where id = #{id}")
    int updateGroupId(String groupId, @Param("id") int id);

    @Update("update set not group_id where id = #{id}")
    int updateNotGroupId(Project project);

    @Update("update *")
    int updateAll(Project project);
}
