package cn.mybatisboost.test;

import cn.mybatisboost.sql.Sql;

public class SqlTest {

    public static void main(String[] args) {
        System.out.println(new Sql().select(Project::getId).from(Project.class).toString());
    }
}
