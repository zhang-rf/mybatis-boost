package me.rfprojects.core.util;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public abstract class MetaObjectUtils {

    public static MetaObject getRealMetaObject(Object target) {
        MetaObject metaObject;
        while ((metaObject = SystemMetaObject.forObject(target)).hasGetter("h")) {
            target = metaObject.getValue("h.target");
        }
        return metaObject;
    }
}
