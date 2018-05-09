# MybatisBoost

Mybatis SQL开发神器MybatisBoost，包含通用Mapper、mybatis语法增强、无感知通用分页和SQL指标与监控功能，使用MybatisBoost来提升你的开发效率吧！

## 快速开始

Tips：基于Spring Boot项目的快速开始

```xml
<dependency>
    <groupId>tech.rfprojects.mybatisboost</groupId>
    <artifactId>mybatis-boost-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

如果你的数据库Table名与POJO类名一致，属性名称命名方式也一致的话，请跳过此部分内容。

MybatisBoost内置有几个常用的表名转换器，现在假设你的表名为T_AnTable，你的POJO类名为AnTable，可以使用如下配置做名称映射。

```
mybatisboost.name-adaptor=cn.mybatisboost.core.adaptor.TPrefixedNameAdaptor
```

如果你的数据库属性命名方式为snake_case，可以使用如下配置做名称映射。

```
mybatis.configuration.map-underscore-to-camel-case=true
```

除了自动映射方案，MybatisBoost同样提供手动映射的方案，现在假设你的表名为T_ThisTable，你的POJO类名为ThatTable，并且属性的名称也不一致，则可以使用JPA提供的标准注解进行手动映射。

```java
@Table(name="T_ThisTable")
public class ThatTable {

    @Id
    private Long id; // 如默认以名称为“id”的字段作为主键，则可以忽略@Id注解
    @Column(name="thisField")
    private String thatField;
}
```

## Mapper使用指南

继承于GenericMapper<T, ID>的Mybatis Mapper接口即自动拥有了GenericMapper的所有功能。默认情况下会使用entity的所有属性进行增删查改，可使用properties，conditionProperties指定参与查询的属性。

```java
public interface GenericMapper<T, ID> {

    int count(T entity, String... conditionProperties);
    T selectOne(T entity, String... conditionProperties);
    List<T> select(T entity, String... conditionProperties);
    List<T> selectWithRowBounds(T entity, RowBounds rowBounds, String... conditionProperties);
    int countAll();
    List<T> selectAll();
    List<T> selectAllWithRowBounds(RowBounds rowBounds);
    T selectById(ID id);
    List<T> selectByIds(ID... ids);
    int insert(T entity, String... properties);
    int batchInsert(List<T> entities, String... properties);
    int insertSelectively(T entity, String... properties);
    int batchInsertSelectively(List<T> entities, String... properties);
    int update(T entity, String... conditionProperties);
    int updatePartially(T entity, String[] properties, String... conditionProperties);
    int updateSelectively(T entity, String... conditionProperties);
    int updatePartiallySelectively(T entity, String[] properties, String... conditionProperties);
    int delete(T entity, String... conditionProperties);
    int deleteByIds(ID... ids);
}
```

## Mybatis语法增强

MybatisBoost目前只包含一个范围参数增强器，直接见实例吧。

关闭Mybatis语法增强
```xml
<select id="selectPostIn">
    SELECT * FROM POST WHERE ID in
    <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

开启Mybatis语法增强
```xml
<select id="selectPostIn">
    SELECT * FROM POST WHERE ID in (#{list})
</select>
```

## 无感知分页

只需在Mapper的方法参数上加上一个RowBounds参数即可实现无感知的物理分页。

```java
List<T> selectAllWithRowBounds(RowBounds rowBounds); // RowBounds内含offset和limit字段
```

## SQL指标与监控

默认情况下所有功能都是不开启的，话不多说，直接上配置，简单易懂。

```
mybatisboost.showQuery=Boolean
mybatisboost.showQueryWithParameters=Boolean
mybatisboost.slowQueryThresholdInMillis=Long
mybatisboost.slowQueryHandler=Class<? extends BiConsumer<String, Long>>
```