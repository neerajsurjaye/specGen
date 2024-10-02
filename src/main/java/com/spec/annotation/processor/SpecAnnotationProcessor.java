package com.spec.annotation.processor;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.spec.annotation.GenGetter;

@SupportedAnnotationTypes({
        "com.spec.annotation.GenGetter"
})
public class SpecAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(GenGetter.class)) {
            generateGetter(element);
        }

        return true;

    }

    private void generateGetter(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            generateGetterForAllField(element);
        } else if (element.getKind() == ElementKind.FIELD) {
            generateGetterForField(element);
        }
    }

    private void generateGetterForAllField(Element element) {
        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                generateGetterForField(enclosed);
            }
        }
    }

    private void generateGetterForField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            return;
        }

        String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        String fieldName = element.getSimpleName().toString();
        String methodName = capitalizeFieldName(fieldName);

        String methodCode = String.format("public %s %s (){return this.%s}", element.asType(), methodName, fieldName);

    }

    private String capitalizeFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
