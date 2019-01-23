package cn.mybatisboost.dsl.condition;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.dsl.MappingUnderscoreToCamelCaseAware;
import cn.mybatisboost.dsl.NameAdaptorAware;
import cn.mybatisboost.util.EntityUtils;
import cn.mybatisboost.util.LambdaUtils;
import cn.mybatisboost.util.SqlUtils;

import java.util.function.Function;

public class ParameterizedColumnCondition
        implements ColumnCondition, MappingUnderscoreToCamelCaseAware, NameAdaptorAware {

    private LambdaUtils.LambdaInfo columnInfo;
    private String symbol;
    private Object[] parameters;
    private boolean mapUnderscoreToCamelCase;
    private NameAdaptor nameAdaptor;

    public <T> ParameterizedColumnCondition(Function<T, ?> column, String symbol, Object... parameters) {
        this.columnInfo = LambdaUtils.getLambdaInfo(column);
        this.symbol = symbol;
        this.parameters = parameters;
    }

    public String getColumn(boolean withTableName) {
        String column = SqlUtils.normalizeColumn
                (columnInfo.getMethodName().replaceFirst("^get", ""), mapUnderscoreToCamelCase);
        if (withTableName) {
            String table = EntityUtils.getTableName(columnInfo.getType(), nameAdaptor);
            return table + "." + column;
        } else {
            return column;
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public Object[] getParameters() {
        return parameters;
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
