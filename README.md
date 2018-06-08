# MybatisBoost [![Build Status](https://www.travis-ci.org/zhang-rf/mybatis-boost.svg?branch=master)](https://www.travis-ci.org/zhang-rf/mybatis-boost)

Mybatis SQL开发神器MybatisBoost，包含通用CrudMapper、Mybatis语法增强、无感知分页、SQL指标与监控和智能Mapper（TODO）功能，使用MybatisBoost来提升你的开发效率！

## 快速开始

基于Spring Boot项目的快速开始。

```xml
<dependency>
    <groupId>tech.rfprojects.mybatisboost</groupId>
    <artifactId>mybatis-boost-spring-boot-starter</artifactId>
    <version>1.0.3</version>
</dependency>
```

如果你的数据库Table名与POJO类名一致，数据库列名与POJO属性名称命名方式也一致的话，那么恭喜你，你已经成功引入了MybatisBoost。

### 此节的剩余部分将展示更为详细的配置内容，如果你不关心的话，可以跳过此部分内容。

MybatisBoost内置有TPrefixedNameAdaptor、SnakeCaseNameAdaptor两个常用的表名转换器，如果内置的表名转换器无法满足你的需求，你也可以基于NameAdaptor接口实现自己的表名转换器。

MybatisBoost默认不使用表名转换器，现在假设你的表名为a_table，你的POJO类名为ATable，请使用如下的配置做名称映射。

```
mybatisboost.name-adaptor=cn.mybatisboost.core.adaptor.SnakeCaseNameAdaptor
```

如果你的数据库列名命名方式为snake_case，请使用Mybatis内置的配置做名称映射。

```
mybatis.configuration.map-underscore-to-camel-case=true
```

#### 除了自动映射方案，MybatisBoost同样提供灵活的手动映射方案。

现在假设你的表名为T_ThisTable，你的POJO类名为ThatTable，并且属性的名称也不一致，则可以使用JPA提供的标准注解进行手动映射。

```java
@Table(name="T_ThisTable")
public class ThatTable {

    @Id // 如默认以名称为“id”的字段作为主键，则可以省略@Id注解
    private Long id;
    @Column(name="thisField")
    private String thatField;

    ...
}
```

到此，MybatisBoost的基础配置就完成了，下面将逐一介绍MybatisBoost的所有功能。

## 通用CrudMapper

继承于CrudMapper&lt;T&gt;的Mybatis Mapper接口即自动拥有了CrudMapper的所有功能，继承时请指明范型“T”代表的POJO类型。

默认情况下，MybatisBoost会使用POJO的所有属性参与查询（以Selectively结尾的方法会忽略值为null的字段），可使用properties参数指定参与插入、更新的属性（如果properties参数的第一个字符串为“!”，则代表排除后续指定的属性），使用conditionProperties参数指定用于WHERE条件的属性，CrudMapper的所有方法如下。

如果担心以字符串的方式指定属性容易出现拼写错误，可以使用MybatisBoost提供的SafeProperty类做运行时的属性拼写检查。

```java
public interface CrudMapper<T> {

    int count(T entity, String... conditionProperties);
    T selectOne(T entity, String... conditionProperties);
    List<T> select(T entity, String... conditionProperties);
    List<T> selectWithRowBounds(T entity, RowBounds rowBounds, String... conditionProperties);
    int countAll();
    List<T> selectAll();
    List<T> selectAllWithRowBounds(RowBounds rowBounds);
    T selectById(Object id);
    List<T> selectByIds(Object... ids);
    int insert(T entity, String... properties);
    int batchInsert(Collection<T> entities, String... properties);
    int insertSelectively(T entity, String... properties);
    int batchInsertSelectively(Collection<T> entities, String... properties);
    int update(T entity, String... conditionProperties);
    int updatePartially(T entity, String[] properties, String... conditionProperties);
    int updateSelectively(T entity, String... conditionProperties);
    int updatePartiallySelectively(T entity, String[] properties, String... conditionProperties);
    int delete(T entity, String... conditionProperties);
    int deleteByIds(Object... ids);
}
```

如果你不需要CrudMapper的所有方法，请继承于GenericMapper&lt;T&gt;接口，然后再把CrudMapper接口中需要的方法复制到你的Mapper里即可。

## Mybatis语法增强

MybatisBoost目前包含数个语法增强器，包括范围参数增强、INSERT增强、UPDATE增强、表名增强和参数增强。默认全部开启，每个语法增强都可以单独使用，也可以联合使用。

### 范围参数语法增强

关闭范围参数语法增强时的SQL编写方式
```xml
<select>
    SELECT * FROM POST WHERE ID IN
    <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

开启范围参数语法增强时的SQL编写方式
```xml
<select>
    SELECT * FROM POST WHERE ID IN #{list}
</select>
```

### INSERT语法增强

INSERT语法增强后，可使用如下3种语法编写INSERT语句。

```sql
INSERT * -- 插入POJO中所有的字段
INSERT column1, column2, column3 -- 只插入POJO中指定的column1、column2、column3三个字段
INSERT NOT column4, column5 -- 插入POJO中除了column4、column5以外的字段
```

### UPDATE语法增强

UPDATE语法增强后，可使用如下几种语法编写UPDATE语句。

```sql
UPDATE SET * -- 更新POJO中所有的字段
UPDATE SET column1, column2, column3 -- 只更新POJO中指定的column1、column2、column3三个字段
UPDATE SET NOT column4, column5 -- 更新POJO中除了column4、column5以外的字段
UPDATE SET * WHERE condition1 = 'condition1' -- 更新POJO中所有的字段，条件为"condition1 = 'condition1'"
```

### 表名语法增强

表名语法增强后，SQL语句中的表名可使用"#t"代替，MybatisBoost会自动替换成正确的表名。

```sql
SELECT * FROM #t
```

### 参数语法增强

参数语法增强后，简单的参数就没有必要再使用Mybatis的"#{}"语法做映射了，也没有必要编写@Param注解来声明参数名称了，MybatisBoost会自动按照参数的声明顺序做出正确的映射。

```sql
@Update("update #t set column1 = ? where condition1 = ?")
int updateState(String a, String b);
```

## 无感知分页

只需要在Mapper方法的参数列表中附加一个Mybatis自带的RowBounds参数即可实现透明的**物理分页**。

```java
List<T> selectAll(RowBounds rowBounds); // RowBounds内含offset和limit字段
```

## SQL指标与监控

默认情况下不开启SQL指标与监控功能。话不多说，直接上配置，简单易懂。

```
mybatisboost.showQuery=boolean // 是否在日志中打印SQL和执行时间
mybatisboost.showQueryWithParameters=boolean // 打印SQL时是否同时打印SQL参数
mybatisboost.slowQueryThresholdInMillis=long // 慢SQL阈值（默认情况下，慢SQL会打印在日志中）
mybatisboost.slowQueryHandler=Class<? extends BiConsumer<String, Long>> // 慢SQL回调处理器（参数一为SQL语句，参数二为执行时间ms），可编写代码实现一些自定义逻辑，比如报警
```

## 欢迎使用

MybatisBoost中没有你想要的功能？亦或是MybatisBoost有BUG？欢迎各位提出issues！