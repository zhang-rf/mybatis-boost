package cn.mybatisboost.test;

import cn.mybatisboost.core.util.SafeProperty;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProjectMapper.class)
public class CrudMapperTest {

    @Autowired
    private ProjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @After
    public void tearDown() {
        jdbcTemplate.execute("delete from project");
    }

    @Test
    public void count() {
        assertEquals(0, mapper.count(new Project()));
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost')");
        assertEquals(1, mapper.count(new Project()));
        assertEquals(1, mapper.count(new Project().setGroupId("cn.mybatisboost")));
        assertEquals(0, mapper.count(new Project().setGroupId("whatever")));
        assertEquals(1, mapper.count(new Project().setGroupId("cn.mybatisboost"),
                SafeProperty.of(Project.class, "groupId")));
        assertEquals(1, mapper.count(new Project().setGroupId("cn.mybatisboost"),
                SafeProperty.of(Project.class, "groupId", "artifactId")));
        assertEquals(0, mapper.count(new Project().setGroupId("cn.mybatisboost").setArtifactId("whatever"),
                SafeProperty.of(Project.class, "groupId", "artifactId")));
        assertEquals(1, mapper.count(new Project().setGroupId("cn.mybatisboost").setArtifactId("whatever"),
                SafeProperty.of(Project.class, "groupId")));
    }

    @Test
    public void selectOne() {
        assertNull(mapper.selectOne(new Project()));
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost')");
        assertEquals("cn.mybatisboost",
                mapper.selectOne(new Project().setGroupId("cn.mybatisboost")).getGroupId());
        assertNull(mapper.selectOne(new Project().setGroupId("whatever")));
    }

    @Test
    public void select() {
    }

    @Test
    public void selectWithRowBounds() {
    }

    @Test
    public void countAll() {
    }

    @Test
    public void selectAll() {
    }

    @Test
    public void selectAllWithRowBounds() {
    }

    @Test
    public void selectById() {
    }

    @Test
    public void selectByIds() {
    }

    @Test
    public void insert() {
    }

    @Test
    public void batchInsert() {
    }

    @Test
    public void insertSelectively() {
    }

    @Test
    public void batchInsertSelectively() {
    }

    @Test
    public void update() {
    }

    @Test
    public void updatePartially() {
    }

    @Test
    public void updateSelectively() {
    }

    @Test
    public void updatePartiallySelectively() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void deleteByIds() {
    }
}