package cn.mybatisboost.test;

import cn.mybatisboost.core.GenericMapper;
import cn.mybatisboost.nosql.Nosql;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectNosqlMapper extends GenericMapper<Project> {

    @Nosql
    int deleteAll();

    @Nosql
    Project selectFirst();

    @Nosql
    List<Project> selectTop2();

    @Nosql
    Project selectAllOffset1Limit1();

    @Nosql
    List<Project> selectByGroupIdAndArtifactId(String groupId, String artifactId);

    @Nosql
    List<Project> selectByGroupIdOrArtifactId(String groupId, String artifactId);

    @Nosql
    List<Project> selectByArtifactIdNot(String artifactId);

    @Nosql
    List<Project> selectAllOrderByGroupIdDesc();
}
