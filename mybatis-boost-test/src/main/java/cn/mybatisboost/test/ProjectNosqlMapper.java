package cn.mybatisboost.test;

import cn.mybatisboost.core.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectNosqlMapper extends GenericMapper<Project> {

    @Mapper
    int deleteAll();

    @Mapper
    Project selectFirst();

    @Mapper
    List<Project> selectTop2();

    @Mapper
    Project selectAllOffset1Limit1();

    @Mapper
    List<Project> selectByGroupIdAndArtifactId(String groupId, String artifactId);

    @Mapper
    List<Project> selectByGroupIdOrArtifactId(String groupId, String artifactId);

    @Mapper
    List<Project> selectByArtifactIdNot(String artifactId);

    @Mapper
    List<Project> selectAllOrderByGroupIdDesc();
}
