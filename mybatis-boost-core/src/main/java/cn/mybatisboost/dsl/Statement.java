package cn.mybatisboost.dsl;

import org.apache.ibatis.session.RowBounds;

public interface Statement {

    String sql();

    Object[] parameters();

    default RowBounds rowBounds() {
        return RowBounds.DEFAULT;
    }
}
