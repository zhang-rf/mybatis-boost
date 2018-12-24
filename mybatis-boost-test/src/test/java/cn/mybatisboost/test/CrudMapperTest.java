package cn.mybatisboost.test;

import cn.mybatisboost.util.SafeProperty;
import org.apache.ibatis.session.RowBounds;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.junit.Assert.*;

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
        assertTrue(mapper.select(new Project()).isEmpty());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost')");
        assertEquals("cn.mybatisboost",
                mapper.select(new Project().setGroupId("cn.mybatisboost")).get(0).getGroupId());
        assertTrue(mapper.select(new Project().setGroupId("whatever")).isEmpty());
    }

    @Test
    public void selectWithRowBounds() {
        assertTrue(mapper.selectWithRowBounds(new Project(), RowBounds.DEFAULT).isEmpty());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost1')");
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost2')");
        assertEquals(2, mapper.selectWithRowBounds(new Project(), RowBounds.DEFAULT).size());
        assertEquals(1, mapper.selectWithRowBounds(new Project(), new RowBounds(1, 1)).size());
        assertEquals("cn.mybatisboost2",
                mapper.selectWithRowBounds(new Project(), new RowBounds(1, 1)).get(0).getGroupId());
    }

    @Test
    public void countAll() {
        assertEquals(0, mapper.countAll());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost')");
        assertEquals(1, mapper.countAll());
    }

    @Test
    public void selectAll() {
        assertEquals(0, mapper.selectAll().size());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost')");
        assertEquals(1, mapper.selectAll().size());
    }

    @Test
    public void selectAllWithRowBounds() {
        assertTrue(mapper.selectAllWithRowBounds(RowBounds.DEFAULT).isEmpty());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost1')");
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost2')");
        assertEquals(2, mapper.selectAllWithRowBounds(RowBounds.DEFAULT).size());
        assertEquals(1, mapper.selectAllWithRowBounds(new RowBounds(1, 1)).size());
        assertEquals("cn.mybatisboost2",
                mapper.selectAllWithRowBounds(new RowBounds(1, 1)).get(0).getGroupId());
    }

    @Test
    public void selectById() {
        assertNull(mapper.selectById(123));
        jdbcTemplate.execute("insert into project (id, group_id) values (123, 'cn.mybatisboost')");
        assertEquals(123, mapper.selectById(123).getId().intValue());
    }

    @Test
    public void selectByIds() {
        assertTrue(mapper.selectByIds(123).isEmpty());
        jdbcTemplate.execute("insert into project (id, group_id) values (123, 'cn.mybatisboost')");
        assertEquals(123, mapper.selectByIds(123).get(0).getId().intValue());
    }

    @Test
    public void selectNullable() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost', 'mybatis-boost')");
        assertNotNull(mapper.selectNullable(null, "mybatis-boost", null));
        assertNull(mapper.selectNullable("cn.mybatisboost", null, null));
        assertNull(mapper.selectNullable(null, null, null));
    }

    @Test
    public void insert() {
        try {
            assertEquals(1, mapper.insert(new Project(null, "mybatis-boost",
                    "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan", null)));
            fail();
        } catch (Exception ignored) {
            // normally, exception would happen because "group_id" column is declared NOT NULL
        }
    }

    @Test
    public void batchInsert() {
        try {
            assertEquals(1, mapper.batchInsert(Collections.singletonList(
                    new Project(null, "mybatis-boost",
                            "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan", null))));
            fail();
        } catch (Exception ignored) {
            // normally, exception would happen because "group_id" column is declared NOT NULL
        }
    }

    @Test
    public void insertSelective() {
        assertEquals(1, mapper.insertSelective(new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan", null)));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
        });
    }

    @Test
    public void batchInsertSelective() {
        assertEquals(1, mapper.batchInsertSelective(Collections.singletonList(
                new Project("cn.mybatisboost", "mybatis-boost",
                        "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan", null))));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
        });
    }

    @Test
    public void update() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        assertEquals(1, mapper.update(new Project().setId(123).setGroupId("cn.mybatisboost2")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost2", resultSet.getString("group_id"));
            assertNull(resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void updatePartial() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        assertEquals(1, mapper.updatePartial(new Project().setId(123).setGroupId("cn.mybatisboost2"),
                SafeProperty.of(Project.class, "groupId")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost2", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void updateSelective() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        assertEquals(1, mapper.updateSelective(new Project().setId(123).setGroupId("cn.mybatisboost2")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost2", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void updatePartialSelective() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        assertEquals(1, mapper.updatePartialSelective(new Project().setId(123).setGroupId("cn.mybatisboost2"),
                SafeProperty.of(Project.class, "groupId")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost2", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void delete() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost')");
        assertEquals(2, mapper.delete(new Project()));
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost')");
        assertEquals(2, mapper.delete(new Project().setArtifactId("mybatis-boost")));
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost')");
        assertEquals(0, mapper.delete(new Project().setGroupId("cn.mybatisboost")));
    }

    @Test
    public void deleteByIds() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost')");
        assertEquals(0, mapper.deleteByIds(0));
        assertEquals(2, mapper.deleteByIds(123, 456));
    }
}