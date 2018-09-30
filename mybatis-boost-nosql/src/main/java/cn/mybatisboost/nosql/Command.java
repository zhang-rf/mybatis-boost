package cn.mybatisboost.nosql;

public enum Command {
    Select("SELECT * FROM"), Count("SELECT COUNT(*) FROM"), Delete("DELETE FROM");

    private String sqlFragment;

    Command(String sqlFragment) {
        this.sqlFragment = sqlFragment;
    }

    public String sqlFragment() {
        return sqlFragment;
    }
}
