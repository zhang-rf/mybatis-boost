package cn.mybatisboost.test;

import cn.mybatisboost.core.GenericMapper;
import cn.mybatisboost.nosql.NosqlQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectNosqlMapper extends GenericMapper<Project> {

    @NosqlQuery
    int deleteAll();

    @NosqlQuery
    Project selectFirst();

    @NosqlQuery
    List<Project> selectTop2();

    @NosqlQuery
    Project selectAllOffset1Limit1();

    @NosqlQuery
    List<Project> selectByGroupIdAndArtifactId(String groupId, String artifactId);

    @NosqlQuery
    List<Project> selectByGroupIdOrArtifactId(String groupId, String artifactId);

    @NosqlQuery
    List<Project> selectByArtifactIdNot(String artifactId);

    @NosqlQuery
    List<Project> selectAllOrderByGroupIdDesc();
}
