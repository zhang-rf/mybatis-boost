package cn.mybatisboost.dsl;

import cn.mybatisboost.test.Project;
import cn.mybatisboost.test.Website;
import org.junit.Test;

import static cn.mybatisboost.dsl.Conditions.*;
import static cn.mybatisboost.dsl.Dsl.select;

@SuppressWarnings("unchecked")
public class DslTest {

    @Test
    public void test() {
        System.out.println(
                select(Project::getId, Project::getGroupId).from(Project.class)
                        .join(Website.class).on(eq(Project::getArtifactId, Website::getHost))
                        .where(not(), $(eq(Project::getGroupId, 1)),
                                or(), ne(Project::getArtifactId, "test"))
                        .orderBy(Project::getId).desc()
                        .offset(5).limit(10)
                        .sql());
    }
}
