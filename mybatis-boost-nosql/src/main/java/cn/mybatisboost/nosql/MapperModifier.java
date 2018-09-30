package cn.mybatisboost.nosql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.ibatis.session.RowBounds;

public class MapperModifier {

    public static void modify(String className, boolean mapUnderscoreToCamelCase) {
        try {
            boolean modified = false;
            CtClass ctClass = ClassPool.getDefault().get(className);
            for (CtMethod ctMethod : ctClass.getMethods()) {
                if (ctMethod.hasAnnotation(Nosql.class)) {
                    MethodNameParser parser = new MethodNameParser
                            (className, ctMethod.getName(), mapUnderscoreToCamelCase);
                    addSelectAnnotation(ctMethod, parser.toSql());
                    if (parser.toRowBounds() != RowBounds.DEFAULT) {
                        addRowBoundsParameter(ctMethod, parser.toRowBounds());
                    }
                    modified = true;
                }
            }
            if (modified) {
                ctClass.toClass(MapperModifier.class.getClassLoader(), MapperModifier.class.getProtectionDomain());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addSelectAnnotation(CtMethod ctMethod, String sql) {
        AnnotationsAttribute attribute = (AnnotationsAttribute)
                ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        ConstPool constPool = attribute.getConstPool();
        Annotation annotation = new Annotation("org.apache.ibatis.annotations.Select", constPool);
        ArrayMemberValue memberValue = new ArrayMemberValue(new StringMemberValue(constPool), constPool);
        memberValue.setValue(new MemberValue[]{new StringMemberValue(sql, constPool)});
        annotation.addMemberValue("value", memberValue);
        attribute.addAnnotation(annotation);
    }

    private static void addRowBoundsParameter(CtMethod ctMethod, RowBounds rowBounds) {
        try {
            CtMethod copiedCtMethod = CtNewMethod.copy(ctMethod, ctMethod.getDeclaringClass(), null);
            ctMethod.addParameter(ClassPool.getDefault().get("org.apache.ibatis.session.rowBounds"));
            ctMethod.setName(ctMethod.getName() + "2");
            StringBuilder body = new StringBuilder(ctMethod.getName() + "2(");
            for (int i = 1; i <= copiedCtMethod.getParameterTypes().length; i++) {
                body.append("$").append(i).append(", ");
            }
            body.append("new org.apache.ibatis.session.rowBounds(")
                    .append(rowBounds.getOffset()).append(", ").append(rowBounds.getLimit()).append("));");
            copiedCtMethod.setBody(body.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
