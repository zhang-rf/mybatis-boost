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
                select(Project::getId)
                        .from(Project.class, Website.class)
                        .where(not(),
                                $(eq(Project::getGroupId, 1)),
                                or(),
                                ne(Project::getArtifactId, Website::getHost))
                        .sql());
    }
}
