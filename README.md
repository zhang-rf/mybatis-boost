# MybatisBoost [![Maven central](https://maven-badges.herokuapp.com/maven-central/cn.mybatisboost/mybatis-boost/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.mybatisboost/mybatis-boost) [![Build Status](https://www.travis-ci.org/zhang-rf/mybatis-boost.svg?branch=master)](https://www.travis-ci.org/zhang-rf/mybatis-boost) [![Coverage Status](https://coveralls.io/repos/github/zhang-rf/mybatis-boost/badge.svg)](https://coveralls.io/github/zhang-rf/mybatis-boost)

Mybatis SQL开发神器MybatisBoost，为Mybatis带来诸多官方没有的新特性，包含通用CrudMapper、Mybatis语法增强、智能方法查询、无感知分页、SQL监控等功能，使用MybatisBoost来提升开发效率，轻松编写SQL代码！

使用MybatisBoost的最低要求：

* JDK 1.8+
* MyBatis 3.0.6+

## 快速开始

基于Spring Boot以及mybatis-spring-boot-starter项目的快速开始。

本章节假设你没有手动创建SqlSessionFactory Bean，否则需要增加一些额外的配置，这部分的内容安排在文档的末尾章节。

Maven:
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
<dependency>
    <groupId>cn.mybatisboost</groupId>
    <artifactId>mybatis-boost-spring-boot-starter</artifactId>
    <version>2.1.3</version>
</dependency>
```

Gradle:
```gradle
compile 'org.mybatis.spring.boot:mybatis-spring-boot-starter:1.3.2'
compile 'cn.mybatisboost:mybatis-boost-spring-boot-starter:2.1.3'
```

如果你的数据库Table名称与项目中的POJO类名一致，Table的列名称与POJO属性的名称命名方式也一致的话（大小写忽略），那么恭喜你，你已经成功引入了MybatisBoost，可以跳过下一章《名称映射》的内容。

> 本文后续内容将使用术语“表”来代表数据库中的表，“列”来代表数据库表中的列，“POJO”来代表表对应的实体类，“属性”和“字段”来代表POJO中的成员变量。

## 名称映射

配置名称映射是为了使Mybatis能自动地找到POJO类对应的表，以及POJO中的属性对应的列，名称映射方案分为自动映射和手动标注两种方案。

关于表名与POJO类名之间的自动映射，MybatisBoost内置有几个常用的表名映射器，如果内置的表名映射器无法满足你的需求，你也可以基于NameAdaptor接口实现自己的表名映射器。

表名映射器|POJO类名|映射到的表名
-|-|-
NoopNameAdaptor|DemoTable|DemoTable
TPrefixedNameAdaptor|DemoTable|T_DemoTable
SnakeCaseNameAdaptor|DemoTable|demo_table

MybatisBoost默认使用NoopNameAdaptor表名映射器，对应的`application.properties`配置如下：

```
mybatisboost.name-adaptor=cn.mybatisboost.core.adaptor.NoopNameAdaptor
```

关于列名与属性名之间的自动映射，MybatisBoost采用了Mybatis内置的MapUnderscoreToCamelCase功能，默认使用CamelCase命名方式。如果你的数据库列名命名方式为snake_case命名方式，请使用如下的`application.properties`配置：

```
mybatis.configuration.map-underscore-to-camel-case=true
```

除了自动映射方案，MybatisBoost同样提供手动标注的方案。

现在假设你的表名为“DEMO_ThisTable”，你的POJO类名为“ThatTable”，表名和POJO类名之间并无任何联系，则可以使用JPA提供的标准注解进行手动标注。

同样地，主键也可以使用JPA提供的标准注解进行手动标注。

关于列名与POJO属性名之间的关系，MybatisBoost采用约定大于配置的思想，不提供手动标注的功能。

```java
@Table(name="DEMO_ThisTable")
public class ThatTable {

    @Id // 默认以名称为“id”的字段作为主键，否则需要使用@Id注解手动标注
    private Long myId;
    private String myField;

    ...
}
```

到此，你已经可以开始使用MybatisBoost了，下面将逐一介绍MybatisBoost的各种功能特性。

## 基础知识

为了使MybatisBoost发挥作用，你的Mybatis Mapper接口类必须直接或间接地继承GenericMapper&lt;T&gt;接口，范型&lt;T&gt;代表此Mapper对应的POJO类。MybatisBoost中提供的所有通用Mapper都是继承于GenericMapper接口的。

## 通用CrudMapper

继承于CrudMapper&lt;T&gt;接口的Mybatis Mapper接口即自动拥有了CrudMapper接口的所有功能。

CrudMapper接口中的方法使用POJO中所有的属性参与CRUD，但不包括以Selective结尾的方法，这些方法会过滤值为null的属性，即POJO中值为null的属性不参与CRUD。

带有`properties`参数的方法，可使用`properties`参数指定参与插入、更新的属性。如果`properties`参数的第一个字符串为“!”，则代表排除后续指定的属性，如“new String[]{"!", "id"}”则代表除“id”以外，其他属性都参与CRUD。

同样地，带有`conditionProperties`参数的方法，可使用`conditionProperties`参数指定用于WHERE条件的属性。

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
    int insertSelective(T entity, String... properties);
    int batchInsertSelective(List<T> entities, String... properties);
    int update(T entity, String... conditionProperties);
    int updatePartial(T entity, String[] properties, String... conditionProperties);
    int updateSelective(T entity, String... conditionProperties);
    int updatePartialSelective(T entity, String[] properties, String... conditionProperties);
    int delete(T entity, String... conditionProperties);
    int deleteByIds(Object... ids);
}
```

如果你不需要CrudMapper接口里的所有方法，可以把CrudMapper接口中所需要的方法复制到你的Mybatis Mapper里即可。（需要把方法上的注解也一并复制。）

## MySQL CrudMapper

除了通用的CrudMapper，MybatisBoost还提供专用于MySQL的MysqlCrudMapper&lt;T&gt;接口，在CrudMapper的基础上，增加了几个支持MySQL特性的方法。

```java
public interface MysqlCrudMapper<T> extends CrudMapper<T> {

    int save(T entity, String... properties);
    int saveSelective(T entity, String... properties);
    int batchSave(List<T> entity, String... properties);
    int batchSaveSelective(List<T> entity, String... properties);
    int replace(T entity, String... properties);
    int replaceSelective(T entity, String... properties);
    int batchReplace(List<T> entity, String... properties);
    int batchReplaceSelective(List<T> entity, String... properties);
}
```

其中，Save方法使用的是MySQL的“ON DUPLICATE KEY UPDATE”特性，Replace方法使用的是“REPLACE INTO”特性。

## Mybatis语法增强

为了使SQL的编写变得简单，MybatisBoost提供了数个Mybatis和SQL语法增强的功能，包括自动参数映射、INSERT语法增强、UPDATE语法增强、表名变量、空值检测和集合参数映射。每个增强都可以单独使用，也可联合使用。

### 自动参数映射

Mybatis设计之中的一个不合理之处，在于舍弃了JDBC原生的参数占位符（即“?”）。显而易见的是，简单的SQL语句根本没有必要使用Mybatis的“#{variable}”语法去做多余的映射，这种麻烦在编写INSERT和UPDATE语句的时候尤为明显。

为此，MybatisBoost恢复了JDBC原生的参数占位符功能，MybatisBoost会自动按照参数的声明顺序做出正确的映射。

```java
@Update("UPDATE table SET column1 = ? WHERE condition1 = ?")
int update(String a, String b);
```

> 自动参数映射目前还不支持嵌套属性，即不支持自动映射到对象中的属性。

### INSERT语法增强

MybatisBoost提供了更为简洁的INSERT语法，使得SQL的编写变得更为简单。

```java
@Insert("INSERT *")
int insertOne1(T entity); // 插入一条记录，插入所有字段

@Insert("INSERT column1, column2, column3")
int insertOne2(T entity); // 插入一条记录，只插入column1、column2、column3三个字段

@Insert("INSERT NOT column4, column5")
int insertOne3(T entity); // 插入一条记录，插入除了column4、column5以外的所有字段

@Insert("INSERT *")
int insertMany(List<T> entities); // 批量插入，插入POJO中的所有字段

...
```

### UPDATE语法增强

同样地，MybatisBoost提供了更为简洁的UPDATE语法。

```java
@Update("UPDATE SET *")
int update1(T entity); // 更新所有字段

@Update("UPDATE SET column1, column2, column3")
int update2(T entity); // 只更新column1、column2、column3三个字段

@Update("UPDATE SET NOT column4, column5")
int update3(T entity); // 更新除了column4、column5以外的所有字段

@Update("UPDATE SET column1, column2 WHERE condition1 = ?")
int update3(String a, String b, String c); // 更新column1、column2两个字段，并且条件是“condition1 = c”

...
```

### 表名变量

在编写SQL语句时，SQL中的表名可使用“#t”代替，MybatisBoost会自动替换成正确的表名。

此功能不仅简化了表名的编写，还使得SQL语句具有了可重用性。

```sql
SELECT * FROM #t
```

### 空值检测

现有一条SQL语句如下：

```sql
SELECT * FROM Post WHERE id = #{id} AND Name != #{name}
```

假设传入的id参数和name参数都为null，则SQL会自动重写为如下的形式：

```sql
SELECT * FROM Post WHERE id IS NULL AND Name IS NOT NULL
```

### 集合参数映射

使用MybatisBoost之前，集合参数的映射方法：

```xml
<select>
    SELECT * FROM Post WHERE id IN
    <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

使用MybatisBoost之后，集合参数的映射方法就如同普通参数一样。

```sql
SELECT * FROM Post WHERE id IN #{list}
```

## 智能方法查询

简单的SQL语句千篇一律，能否不再编写那些显而易见的SQL语句呢？答案是肯定的。

```java
public interface PostMapper extends GenericMapper<Post> {

    @org.apache.ibatis.annotations.Mapper
    List<Post> selectByPostIdAndPostDateBw(int a, Date b, Date c);
}
```

以上代码片段是一个简单的方法查询，MybatisBoost会自动识别并生成对应的SQL语句。

```sql
SELECT * FROM #t WHERE PostId = ? AND PostDate BETWEEN ? AND ?
```

只要以Mybatis的@Mapper注解标记的接口方法，MybatisBoost都会智能的生成相应SQL语句，让你的双手解放于千篇一律的简单SQL语句。

> 低版本的Mybatis没有@Mapper注解，可以使用MybatisBoost提供的@cn.mybatisboost.support.Mapper注解代替。

下面我们就以“selectByPostIdAndPostDateBw”来分析下如何编写智能方法查询，分解后的单词如下：select By PostId And PostDate Bw。其中“select”称为“方法词”，“By”称为“辅助词”，“PostId”和“PostDatae”为POJO中的属性，“And”和“Bw”（BETWEEN的缩写）为SQL关键字，其中，方法词和辅助词都是必须的，其他的都为可选项。

目前支持的方法词：select、count、delete。

目前支持的关键字：

关键字|缩写|对应的SQL片段
-|-|-
And|And|AND
Or|Or|OR
Is|Is|= ?
Equals|E|= ?
Between|Bw|BETWEEN ? AND ?
NotBetween|Nbw|NOT BETWEEN ? AND ?
LessThan|Lt|< ?
LessThanEqual|Lte|<= ?
GreaterThan|Gt|> ?
GreaterThanEqual|Gte|>= ?
After|Af|> ?
Before|Bf|< ?
IsNull|N|IS| NULL
IsNotNull|Nn|IS NOT NULL
IsEmpty|E|= ''
IsNotEmpty|Ne|!= ''
Like|L|LIKE ?
NotLike|Nl|NOT LIKE ?
OrderBy|Ob|ORDER BY
Not|Not|!= ?
In|In|IN| ?
NotIn|Ni|NOT IN ?
IsTrue|T|= TRUE
IsFalse|F|= FALSE
Asc|Asc|ASC
Desc|Desc|DESC

同时，智能查询方法还支持分页功能：

```java
public interface PostMapper extends GenericMapper<Post> {

    @Mapper
    List<Post> selectAllOffset10Limit100();

    @Mapper
    List<Post> selectTop3();

    @Mapper
    Post selectFirst();
}
```

## 无感知分页

Mybatis本身其实已经提供了分页的功能，可惜它的实现并不优雅。为此，MybatisBoost在使用方法不变的前提下，透明的修改了实现，做到了真正的`物理分页`。

```java
List<T> selectAll(RowBounds rowBounds); // RowBounds内含offset和limit字段
```

> 目前暂时只支持MySQL和PostgreSQL数据库，后续支持敬请期待。

## SQL指标与监控

默认情况下不开启SQL指标与监控功能。话不多说，直接上配置，简单易懂。

```
# 开启SQL指标与监控功能
mybatisboost.metric.enabled=true
# 在日志中打印SQL和执行时间
mybatisboost.showQuery=true

# 以下配置为可选配置

# 打印SQL时是否同时打印SQL参数
mybatisboost.showQueryWithParameters=boolean
# 慢SQL阈值（默认情况下，慢SQL会打印在日志中）
mybatisboost.slowQueryThresholdInMillis=long
# 慢SQL回调处理器（参数一为SQL语句，参数二为执行时间ms），可编写代码实现一些自定义逻辑，比如报警
mybatisboost.slowQueryHandler=Class<? extends BiConsumer<String, Long>>
```

## 欢迎使用

光看文档太抽象？mybatis-boost-test模块下有各种使用case，欢迎各位参考。

MybatisBoost中没有你想要的功能？亦或是MybatisBoost有BUG？欢迎各位提出issues！