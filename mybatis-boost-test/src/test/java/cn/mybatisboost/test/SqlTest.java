package cn.mybatisboost.test;

import cn.mybatisboost.sql.Sql;

import java.util.ArrayList;

public class SqlTest {

    public static void main(String[] args) {
        System.out.println(new Sql().
                select(Project::getId).from(Project.class, Website.class).where(Sql.newCondition().eq(Project::getId, Project::getGroupId).or().eq(Project::getId, 123))
                .and().where(Sql.newCondition().ne(Project::getArtifactId, 456).and().in(Project::getWebsite, new ArrayList<>()).and().isNotEmpty(Project::getWebsite).and().between(Project::getGroupId, 1, 2))
                .toString());
    }
}
