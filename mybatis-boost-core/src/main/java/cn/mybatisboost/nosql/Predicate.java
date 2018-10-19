package cn.mybatisboost.nosql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Predicate {
    And("And", "AND", false),
    Or("Or", "OR", false),
    Between("Bw", "BETWEEN ? AND ?", true),
    Lessthan("Lt", "< ?", true),
    Lessthanequal("Lte", "<= ?", true),
    Greaterthan("Gt", "> ?", true),
    Greaterthanequal("Gte", ">= ?", true),
    Isnull("Null", "IS NULL", true),
    Isempty("Empty", "= ''", true),
    Like("Like", "LIKE ?", true),
    Orderby("Ob", "ORDER BY", false),
    Not("Not", "NOT", false),
    In("In", "IN ?", true),
    True("True", "= TRUE", true),
    False("False", "= FALSE", true),
    Asc("Asc", "ASC", true),
    Desc("Desc", "DESC", true);

    private static Map<String, Predicate> aliasMap = new HashMap<>();
    private String alias, sqlFragment;
    private boolean conditional;

    static {
        Arrays.stream(values()).forEach(predicate -> aliasMap.put(predicate.alias, predicate));
    }

    Predicate(String alias, String sqlFragment, boolean conditional) {
        this.alias = alias;
        this.sqlFragment = sqlFragment;
        this.conditional = conditional;
    }

    public String alias() {
        return alias;
    }

    public String sqlFragment() {
        return sqlFragment;
    }

    public boolean conditional() {
        return conditional;
    }

    public static Predicate of(String name) {
        return aliasMap.compute(name, (k, v) -> v != null ? v : valueOf(name));
    }
}
