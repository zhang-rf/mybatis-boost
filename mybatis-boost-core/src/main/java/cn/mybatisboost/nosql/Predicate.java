package cn.mybatisboost.nosql;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Predicate {
    And("And", "AND", false),
    Or("Or", "OR", false),
    Is("Is", "= ?", true),
    Equals("E", "= ?", true),
    Between("Bw", "BETWEEN ? AND ?", true),
    NotBetween("Nbw", "NOT BETWEEN ? AND ?", true),
    LessThan("Lt", "< ?", true),
    LessThanEqual("Lte", "<= ?", true),
    GreaterThan("Gt", "> ?", true),
    GreaterThanEqual("Gte", ">= ?", true),
    After("Af", "> ?", true),
    Before("Bf", "< ?", true),
    IsNull("N", "IS NULL", true),
    IsNotNull("Nn", "IS NOT NULL", true),
    IsEmpty("E", "= ''", true),
    IsNotEmpty("Ne", "!= ''", true),
    Like("L", "LIKE ?", true),
    NotLike("Nl", "NOT LIKE ?", true),
    OrderBy("Ob", "ORDER BY", false),
    Not("Not", "!= ?", true),
    In("In", "IN ?", true),
    NotIn("Ni", "NOT IN ?", true),
    IsTrue("T", "= TRUE", true),
    IsFalse("F", "= FALSE", true),
    Asc("Asc", "ASC", true),
    Desc("Desc", "DESC", true);

    private static List<String> keywords;
    private static Map<String, Predicate> aliasMap = new HashMap<>();
    private String abbr, sqlFragment;
    private boolean containsParameters;

    static {
        keywords = Stream.concat(Arrays.stream(values()).map(Predicate::name), Arrays.stream(values()).map(Predicate::alias))
                .distinct().sorted(Comparator.comparingInt(it -> -it.length())).collect(Collectors.toList());
        keywords = Collections.unmodifiableList(keywords);
        Arrays.stream(values()).forEach(it -> aliasMap.put(it.abbr, it));
    }

    Predicate(String abbr, String sqlFragment, boolean containsParameters) {
        this.abbr = abbr;
        this.sqlFragment = sqlFragment;
        this.containsParameters = containsParameters;
    }

    public String alias() {
        return abbr;
    }

    public String sqlFragment() {
        return sqlFragment;
    }

    public boolean containsParameters() {
        return containsParameters;
    }

    public static Predicate of(String name) {
        return aliasMap.compute(name, (k, v) -> v != null ? v : valueOf(name));
    }

    public static List<String> keywords() {
        return keywords;
    }
}
