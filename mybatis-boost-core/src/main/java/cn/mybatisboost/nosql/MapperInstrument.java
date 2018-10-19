package cn.mybatisboost.nosql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.ibatis.session.RowBounds;

import java.util.UUID;

public class MapperInstrument {

    public static boolean modify(String className, boolean mapUnderscoreToCamelCase) {
        try {
            boolean modified = false;
            CtClass ctClass = ClassPool.getDefault().get(className);
            for (CtMethod ctMethod : ctClass.getMethods()) {
                if (ctMethod.hasAnnotation(Nosql.class)) {
                    MethodNameParser parser =
                            new MethodNameParser(ctMethod.getName(), "#t", mapUnderscoreToCamelCase);
                    addSelectAnnotation(ctMethod, parser.toSql());
                    addRowBoundsParameter(ctMethod, parser.toRowBounds());
                    modified = true;
                }
            }
            if (modified) {
                ctClass.toClass(MapperInstrument.class.getClassLoader(), MapperInstrument.class.getProtectionDomain());
            }
            return modified;
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
        if (rowBounds == RowBounds.DEFAULT) return;
        try {
            String newMethodName = ctMethod.getName() + "$" +
                    UUID.randomUUID().toString().replace("-", "");
            CtMethod ctNewMethod = CtNewMethod.copy(ctMethod, newMethodName, ctMethod.getDeclaringClass(), null);
            ctNewMethod.addParameter(ClassPool.getDefault().get("org.apache.ibatis.session.RowBounds"));
            ctNewMethod.getMethodInfo().addAttribute
                    (ctMethod.getMethodInfo().removeAttribute(AnnotationsAttribute.visibleTag));
            if (rowBounds.getLimit() == 1) {
                MethodInfo methodInfo = ctNewMethod.getMethodInfo();
                String descriptor = methodInfo.getDescriptor();
                String returnType = descriptor.substring(descriptor.lastIndexOf(')') + 1);
                methodInfo.setDescriptor(descriptor =
                        descriptor.substring(0, descriptor.length() - returnType.length()) + "Ljava/util/List;");
                ctNewMethod.setGenericSignature
                        (descriptor.substring(0, descriptor.length() - 1) + "<" + returnType + ">;");
            }
            ctMethod.getDeclaringClass().addMethod(ctNewMethod);
            if (rowBounds.getLimit() == 1) {
                String body = "{ java.util.List list = %s($$, new org.apache.ibatis.session.RowBounds(%s, %s));" +
                        "return !list.isEmpty() ? list.get(0) : null; }";
                body = String.format(body, newMethodName, rowBounds.getOffset(), rowBounds.getLimit());
                ctMethod.setBody(body);
            } else {
                String body = String.format("{ return %s($$, new org.apache.ibatis.session.RowBounds(%s, %s)); }",
                        newMethodName, rowBounds.getOffset(), rowBounds.getLimit());
                ctMethod.setBody(body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
