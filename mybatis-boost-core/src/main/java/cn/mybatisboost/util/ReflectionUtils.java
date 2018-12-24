package cn.mybatisboost.util;

import java.lang.reflect.AccessibleObject;

public abstract class ReflectionUtils {

    public static <T extends AccessibleObject> T makeAccessible(T accessibleObject) {
        accessibleObject.setAccessible(true);
        return accessibleObject;
    }
}
