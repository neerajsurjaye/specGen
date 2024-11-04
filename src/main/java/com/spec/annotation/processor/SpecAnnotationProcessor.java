package com.spec.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.spec.annotation.GenGetter;

/*
* This Class will process the custom annotation during compile time.
* 
* AbstractProcessor helps in processing annotation during compile time
* 
* @SupportedAnnotationTypes defines which annotation is supported by the Abstract processor
*/
@SupportedAnnotationTypes({
        "com.spec.annotation.GenGetter"
})
public class SpecAnnotationProcessor extends AbstractProcessor {

    /*
     * The following method is called during compile time by the java compiler.
     * 
     * Set<? extends TypeElement> is the set of annotations
     * 
     * RoundEnvironment contains the elements annotated
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("Processor called");
        // generate getter for elements annotated with @Getter annotation
        for (Element element : roundEnv.getElementsAnnotatedWith(GenGetter.class)) {
            generateGetter(element);
        }

        return true;

    }

    /*
     * Common method for generating getters for Class or Fields
     */
    private void generateGetter(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            generateGetterForAllField(element);
        } else if (element.getKind() == ElementKind.FIELD) {
            generateGetterForField(element);
        }
    }

    /*
     * Generate getter for class level @Getter annotation
     */
    private void generateGetterForAllField(Element element) {
        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                generateGetterForField(enclosed);
            }
        }
    }

    /*
     * Generate getter for method level @Getter annotation
     */
    private void generateGetterForField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            return;
        }

        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        // todo remove this gen from end
        String className = enclosingElement.getQualifiedName().toString() + "gen";
        String fieldName = element.getSimpleName().toString();
        String capitalizedFieldName = capitalizeFieldName(fieldName);
        String getterName = "get" + capitalizedFieldName;

        // flag passed as 0 means I dont want any special features.
        ClassWriter classWriter = new ClassWriter(0);

        // internally classes are represented using / instead of ".".
        String classInternalName = className.replace(".", "/");

        // todo find if the opcode version can be changed
        classWriter.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                classInternalName,
                null,
                "java/lang/Object",
                null);

        MethodVisitor methodVisitor = classWriter.visitMethod(
                Opcodes.ACC_PUBLIC,
                getterName,
                "()" + getDescriptor(element.asType()),
                null,
                null);

        methodVisitor.visitCode();

        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);

        methodVisitor.visitFieldInsn(
                Opcodes.GETFIELD,
                classInternalName,
                fieldName,
                getDescriptor(element.asType()));

        methodVisitor.visitInsn(Opcodes.RETURN);

        methodVisitor.visitMaxs(1, 1);

        methodVisitor.visitEnd();

        classWriter.visitEnd();

        writeClass(classInternalName, classWriter.toByteArray());

    }

    private String capitalizeFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    // todo visit this again and get info about type discriptor
    private String getDescriptor(TypeMirror type) {

        if (type.getKind().isPrimitive()) {
            switch (type.getKind()) {
                case INT:
                    return "I";
                case BOOLEAN:
                    return "Z";
                case CHAR:
                    return "C";
                case BYTE:
                    return "B";
                case SHORT:
                    return "S";
                case LONG:
                    return "J";
                case FLOAT:
                    return "F";
                case DOUBLE:
                    return "D";
                default:
                    throw new IllegalArgumentException("Unknown primitve type : " + type);

            }
        } else if (type.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) type;
            return "[" + getDescriptor(arrayType.getComponentType());
        } else {
            String internalName = ((DeclaredType) type).asElement().toString().replace(".", "/");
            return "L" + internalName + ";";
        }

    }

    private void writeClass(String className, byte[] bytecode) {
        try {
            JavaFileObject fileObject = processingEnv.getFiler().createClassFile(className);
            try (Writer writer = fileObject.openWriter()) {
                writer.write(new String(bytecode));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
