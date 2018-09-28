package cn.mybatisboost.nosql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

public class MapperModifier {

    public static void modify(String className) {
        try {
            boolean modified = false;
            CtClass ctClass = ClassPool.getDefault().get(className);
            for (CtMethod ctMethod : ctClass.getMethods()) {
                if (ctMethod.hasAnnotation(Nosql.class)) {
                    AnnotationsAttribute attribute = (AnnotationsAttribute)
                            ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
                    ConstPool constPool = attribute.getConstPool();
                    Annotation annotation = new Annotation("org.apache.ibatis.annotations.Select", constPool);
                    ArrayMemberValue memberValue = new ArrayMemberValue(new StringMemberValue(constPool), constPool);
                    memberValue.setValue(new MemberValue[]{new StringMemberValue("SELECT now()", constPool)});
                    annotation.addMemberValue("value", memberValue);
                    attribute.addAnnotation(annotation);
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
}
