package cn.mybatisboost.test;

import cn.mybatisboost.dsl.Dsl;

import static cn.mybatisboost.dsl.Conditions.*;

@SuppressWarnings("unchecked")
public class SqlTest {

    public static void main(String[] args) {
        System.out.println(Dsl.select(Project::getId).from(Project.class).where(not(), $(eq(Project::getGroupId, 1)), or(), ne(Project::getArtifactId, 2)).sql());
    }
}
