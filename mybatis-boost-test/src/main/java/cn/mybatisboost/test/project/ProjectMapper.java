package cn.mybatisboost.test.project;

import cn.mybatisboost.mapper.CrudMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper extends CrudMapper<Project> {

    @Select("select * from #t")
    List<Project> selectFromT();

    @Select("select * from project where id in #{ids}")
    List<Project> selectRange(@Param("ids") List<Integer> ids);

    @Select("select * from #t where id in ?")
    List<Project> selectRange2(List<Integer> list);

    @Select("select * from #t where id = ?")
    Project selectOneFromT(Integer id);

    @Select("select * from #t where group_id != ? and artifact_id = ? and scm = ?")
    Project selectNullable(String groupId, String artifactId, String scm);

    @Insert("insert *")
    @Options(useGeneratedKeys = true)
    int insertSome(List<Project> list);

    @Insert("insert group_id")
    @Options(useGeneratedKeys = true)
    int insertOne1(Project project);

    @Insert("insert NOT artifact_id")
    @Options(useGeneratedKeys = true)
    int insertOne2(Project project);

    @Update("update set group_id where id = ?")
    int updateGroupId(String groupId, int id);

    @Update("update set not group_id")
    int updateNotGroupId(Project project);

    @Update("update set *")
    int updateAll(Project project);
}
