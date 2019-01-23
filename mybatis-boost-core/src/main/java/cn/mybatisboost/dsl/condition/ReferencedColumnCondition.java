package cn.mybatisboost.dsl.condition;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.dsl.MappingUnderscoreToCamelCaseAware;
import cn.mybatisboost.dsl.NameAdaptorAware;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;
import cn.mybatisboost.util.SqlUtils;

import java.util.function.Function;

public class ReferencedColumnCondition implements ColumnCondition, MappingUnderscoreToCamelCaseAware, NameAdaptorAware {

    private LambdaUtils.LambdaInfo columnInfo;
    private String symbol;
    private LambdaUtils.LambdaInfo referencedColumnInfo;
    private boolean mapUnderscoreToCamelCase;
    private NameAdaptor nameAdaptor;

    public <T1, T2> ReferencedColumnCondition(Function<T1, ?> column, String symbol, Function<T2, ?> referencedColumn) {
        this.columnInfo = LambdaUtils.getLambdaInfo(column);
        this.symbol = symbol;
        this.referencedColumnInfo = LambdaUtils.getLambdaInfo(referencedColumn);
    }

    public String getColumn() {
        String column = SqlUtils.normalizeColumn
                (columnInfo.getMethodName().replaceFirst("^get", ""), mapUnderscoreToCamelCase);
        String table = EntityUtils.getTableName(columnInfo.getType(), nameAdaptor);
        return table + "." + column;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getReferencedColumn() {
        String column = SqlUtils.normalizeColumn
                (referencedColumnInfo.getMethodName().replaceFirst("^get", ""),
                        mapUnderscoreToCamelCase);
        String table = EntityUtils.getTableName(referencedColumnInfo.getType(), nameAdaptor);
        return table + "." + column;
    }

    @Override
    public void setMappingUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    @Override
    public void setNameAdaptor(NameAdaptor nameAdaptor) {
        this.nameAdaptor = nameAdaptor;
    }
}
