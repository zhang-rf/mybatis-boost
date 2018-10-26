package cn.mybatisboost.nosql;

import java.util.Arrays;

public enum Command {
    Select("SELECT * FROM"), Count("SELECT COUNT(*) FROM"), Delete("DELETE FROM");

    private String sqlFragment;

    Command(String sqlFragment) {
        this.sqlFragment = sqlFragment;
    }

    public String sqlFragment() {
        return sqlFragment;
    }

    public static Command of(String name) {
        return Arrays.stream(values()).filter(it -> name.startsWith(it.name())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal command type"));
    }
}
