package cn.mybatisboost.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LambdaUtils implements MethodInterceptor {

    private static Pattern CLASS_CAST_PATTERN = Pattern.compile("cannot be cast to (.+)$");
    private LambdaInfo lambdaInfo;

    private LambdaUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T, R> LambdaInfo getLambdaInfo(Function<T, R> function) {
        Enhancer enhancer = new Enhancer();
        LambdaUtils holder = new LambdaUtils();
        enhancer.setCallback(holder);
        try {
            function.apply((T) enhancer.create());
        } catch (ClassCastException e) {
            Matcher matcher = CLASS_CAST_PATTERN.matcher(e.getMessage());
            if (matcher.find()) {
                try {
                    enhancer.setSuperclass(holder.getClass().getClassLoader().loadClass(matcher.group(1)));
                    function.apply((T) enhancer.create());
                } catch (ClassNotFoundException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
        return holder.lambdaInfo;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
        this.lambdaInfo = new LambdaInfo(method.getDeclaringClass(), method.getName());
        return null;
    }

    public static class LambdaInfo {

        private Class<?> type;
        private String methodName;

        public LambdaInfo(Class<?> type, String methodName) {
            this.type = type;
            this.methodName = methodName;
        }

        public Class<?> getType() {
            return type;
        }

        public String getMethodName() {
            return methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LambdaInfo that = (LambdaInfo) o;
            return type.equals(that.type) &&
                    methodName.equals(that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, methodName);
        }

        @Override
        public String toString() {
            return "LambdaInfo{" +
                    "type=" + type +
                    ", methodName='" + methodName + '\'' +
                    '}';
        }
    }
}
