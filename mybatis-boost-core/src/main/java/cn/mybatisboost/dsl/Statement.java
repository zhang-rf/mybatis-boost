package cn.mybatisboost.dsl;

public interface Statement {

    String sql();

    Object[] parameters();
}
