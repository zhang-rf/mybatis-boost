# MybatisBoost [![Maven central](https://maven-badges.herokuapp.com/maven-central/cn.mybatisboost/mybatis-boost/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.mybatisboost/mybatis-boost) [![Build Status](https://www.travis-ci.org/zhang-rf/mybatis-boost.svg?branch=master)](https://www.travis-ci.org/zhang-rf/mybatis-boost) [![Coverage Status](https://coveralls.io/repos/github/zhang-rf/mybatis-boost/badge.svg)](https://coveralls.io/github/zhang-rf/mybatis-boost)

Mybatis SQL开发神器MybatisBoost，包含通用CrudMapper、Mybatis语法增强、无感知分页、SQL指标与监控功能，使用MybatisBoost来提升开发效率，内聚SQL代码！

## 快速开始

基于Spring Boot项目的快速开始。

```xml
<dependency>
    <groupId>cn.mybatisboost</groupId>
    <artifactId>mybatis-boost-spring-boot-starter</artifactId>
    <version>2.0.2</version>
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

到此，MybatisBoost的基础配置就完成了，下面将逐一介绍MybatisBoost的各种功能。

## 通用CrudMapper

继承于CrudMapper&lt;T&gt;的Mybatis Mapper接口即自动拥有了CrudMapper的所有功能，继承时请指明范型“T”代表的POJO类型。

默认情况下，MybatisBoost使用POJO的所有属性参与CRUD，以Selectively结尾的方法会过滤值为null的字段，即POJO中值为null的字段不参与CRUD。

带有properties参数的方法，可使用properties参数指定参与插入、更新的属性。如果properties参数的第一个字符串为“!”，则代表排除后续指定的属性，如“new String[]{"!", "id"}”则代表除“id”以外，其他属性都参与CRUD。
带有conditionProperties参数的方法，可使用conditionProperties参数指定用于WHERE条件的属性。

需要注意的是，properties和conditionProperties使用的都是POJO中的字段名，而不是数据库Table中的列名。
如果担心以字符串的方式指定属性容易出现拼写错误，可以使用MybatisBoost提供的SafeProperty类做运行时的属性拼写检查。

CrudMapper的所有方法如下

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
    int batchInsert(List<T> entities, String... properties);
    int insertSelectively(T entity, String... properties);
    int batchInsertSelectively(List<T> entities, String... properties);
    int update(T entity, String... conditionProperties);
    int updatePartially(T entity, String[] properties, String... conditionProperties);
    int updateSelectively(T entity, String... conditionProperties);
    int updatePartiallySelectively(T entity, String[] properties, String... conditionProperties);
    int delete(T entity, String... conditionProperties);
    int deleteByIds(Object... ids);
}
```

如果你不需要CrudMapper的所有方法，请继承于GenericMapper&lt;T&gt;接口，然后再把CrudMapper接口所需的方法复制到你的Mapper里即可。

## Mybatis语法增强

MybatisBoost目前包含数个语法增强器，包括范围参数增强、INSERT增强、UPDATE增强、表名增强、参数增强和空值增强。默认全部开启，每个语法增强都可以单独使用，也可以联合使用。

大部分的语法增强都依赖于GenericMapper<T>接口，所以使用了语法增强的Mapper必须继承于CrudMapper<T>或GenericMapper<T>接口。

### 范围参数增强

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
```sql
SELECT * FROM POST WHERE ID IN #{list}
```

Mapper接口编写方式
```java
List<Post> select(List<Integer> list);
```

多个范围参数的情况下，需要使用"@Param"注解指定参数名称
```sql
SELECT * FROM POST WHERE ID IN #{ids} AND Name IN #{names}
```

```java
List<Post> select(@Param("ids") List<Integer> ids, @Param("names") List<Integer> names);
```

需要注意的是，范围参数增强无法和MyBatis提供的"<foreach>"一起使用，使用"<foreach>"后，范围参数增强将失效。

### INSERT增强

INSERT语法增强后，可使用如下3种语法编写INSERT语句，INSERT语法增强同样支持批量插入。
需要注意的是，INSERT语句中所使用的都是Table中的列名，而不是POJO中的字段名。

```sql
INSERT * -- 插入Table中所有的列
INSERT column1, column2, column3 -- 只插入Table中column1、column2、column3三个列
INSERT NOT column4, column5 -- 插入Table中除了column4、column5以外的列
```

Mapper接口编写方式
```java
int insertOne(T entity); // 插入一条记录
int insertMany(List<T> entities); // 批量插入
```

### UPDATE增强

UPDATE语法增强后，可使用如下几种语法编写UPDATE语句。
同样，UPDATE语句中所使用的都是Table中的列名，而不是POJO中的字段名。

```sql
UPDATE SET * -- 更新Table中所有的列
UPDATE SET column1, column2, column3 -- 只更新Table中column1、column2、column3三个列
UPDATE SET NOT column4, column5 -- 更新Table中除了column4、column5以外的列
UPDATE SET * WHERE condition1 = 'condition1' -- 更新Table中所有的字段，条件为"condition1 = 'condition1'"
```

UPDATE语法增强的Mapper接口编写方式有如下的两种
```java
int update(T entity);
int update(@Param("property1") String property1, @Param("property2") String property2, @Param("property3") String property3);
```

### 表名增强

表名语法增强后，SQL语句中的表名可使用"#t"代替，MybatisBoost会自动替换成正确的表名。

```sql
SELECT * FROM #t
```

### 参数增强

参数语法增强后，简单的参数就没有必要再使用Mybatis的"#{}"语法做映射了，也没有必要编写@Param注解来声明参数名称了，MybatisBoost会自动按照参数的声明顺序做出正确的映射。
（参数语法增强不支持嵌套属性，即不支持自动映射到对象中的属性。）

```java
@Update("update #t set column1 = ? where condition1 = ?")
int updateState(String a, String b);
```

### 空值增强

空值语法增强后，在传入的参数为null的情况下，会自动重写SQL相应条件部分为“IS NULL”或“IS NOT NULL”

```sql
SELECT * FROM Post WHERE Id = #{id} AND Name != #{name}
```

假定传入的id和name参数都为null，则SQL会自动重写为如下的形式

```sql
SELECT * FROM Post WHERE Id IS NULL AND Name IS NOT NULL
```

## 无感知分页

只需要在Mapper方法的参数列表中附加一个Mybatis自带的RowBounds参数即可实现透明的**物理分页**。暂时只支持MySQL和PostgreSQL，后续会支持更多的数据库。

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

光看文档太抽象？mybatis-boost-test模块下有各种使用case，欢迎各位检阅测试代码。

MybatisBoost中没有你想要的功能？亦或是MybatisBoost有BUG？欢迎各位提出issues！