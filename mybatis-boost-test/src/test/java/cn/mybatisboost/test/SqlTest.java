package cn.mybatisboost.test;

import cn.mybatisboost.sql.Sql;

public class SqlTest {

    public static void main(String[] args) {
        System.out.println(new Sql().
                select(Project::getId).from(Project.class).where(Sql.newCondition().eq(Project::getId, Project::getGroupId).or().eq(Project::getId, 123))
                .toString());
    }
}
