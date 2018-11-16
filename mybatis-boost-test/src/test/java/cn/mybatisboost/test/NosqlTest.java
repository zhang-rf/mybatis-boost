package cn.mybatisboost.test;

import cn.mybatisboost.core.GenericMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * We use reflection to test Nosql feature
 * mainly because we mustn't but have to load mapper classes before there were scanned by Mybatis
 */
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = GenericMapper.class)
@SuppressWarnings("unchecked")
public class NosqlTest {

    @Autowired
    @Qualifier("projectNosqlMapper")
    private GenericMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost1')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost2')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (789, 'cn.mybatisboost3', 'mybatis-boost3')");
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("delete from project");
    }

    @Test
    public void deleteAll() throws Exception {
        Method deleteAll = mapper.getClass().getDeclaredMethod("deleteAll");
        assertEquals(3, deleteAll.invoke(mapper));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(0, resultSet.getRow());
        });
    }

    @Test
    public void selectFirst() throws Exception {
        Method selectFirst = mapper.getClass().getDeclaredMethod("selectFirst");
        assertEquals(123, (int) ((Project) selectFirst.invoke(mapper)).getId());
    }

    @Test
    public void selectTop2() throws Exception {
        Method selectTop2 = mapper.getClass().getDeclaredMethod("selectTop2");
        List<Project> list = (List<Project>) selectTop2.invoke(mapper);
        assertEquals(2, list.size());
        assertEquals(123, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
    }

    @Test
    public void selectAllOffset1Limit1() throws Exception {
        Method selectAllOffset1Limit1 = mapper.getClass().getDeclaredMethod("selectAllOffset1Limit1");
        assertEquals(456, (int) ((Project) selectAllOffset1Limit1.invoke(mapper)).getId());
    }

    @Test
    public void selectByGroupIdAndArtifactId() throws Exception {
        Method selectByGroupIdAndArtifactId = mapper.getClass().getDeclaredMethod("selectByGroupIdAndArtifactId", String.class, String.class);
        assertEquals(123, (int) ((Project) selectByGroupIdAndArtifactId.invoke(mapper, "cn.mybatisboost1", "mybatis-boost1")).getId());
    }

    @Test
    public void selectByGroupIdOrArtifactId() throws Exception {
        Method selectByGroupIdOrArtifactId = mapper.getClass().getDeclaredMethod("selectByGroupIdOrArtifactId", String.class, String.class);
        List<Project> list = (List<Project>) selectByGroupIdOrArtifactId.invoke(mapper, "cn.mybatisboost1", "mybatis-boost2");
        assertEquals(2, list.size());
        assertEquals(123, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
    }

    @Test
    public void selectByNotArtifactId() throws Exception {
        Method selectByArtifactIdNot = mapper.getClass().getDeclaredMethod("selectByArtifactIdNot", String.class);
        List<Project> list = (List<Project>) selectByArtifactIdNot.invoke(mapper, "mybatis-boost1");
        assertEquals(2, list.size());
        assertEquals(456, (int) list.get(0).getId());
        assertEquals(789, (int) list.get(1).getId());
    }

    @Test
    public void selectAllOrderByGroupIdDesc() throws Exception {
        Method selectAllOrderByGroupIdDesc = mapper.getClass().getDeclaredMethod("selectAllOrderByGroupIdDesc");
        List<Project> list = (List<Project>) selectAllOrderByGroupIdDesc.invoke(mapper);
        assertEquals(3, list.size());
        assertEquals(789, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
        assertEquals(123, (int) list.get(2).getId());
    }
}
