package cn.mybatisboost.nosql;

import java.util.Arrays;

public enum Method {
    Select("SELECT * FROM"), Count("SELECT COUNT(*) FROM"), Delete("DELETE FROM");

    private String sqlFragment;

    Method(String sqlFragment) {
        this.sqlFragment = sqlFragment;
    }

    public String sqlFragment() {
        return sqlFragment;
    }

    public static Method of(String name) {
        return Arrays.stream(values()).filter(it -> name.startsWith(it.name())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal method type"));
    }
}
