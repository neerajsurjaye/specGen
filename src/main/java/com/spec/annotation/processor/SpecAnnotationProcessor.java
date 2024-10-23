package com.spec.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

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

        String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        String fieldName = element.getSimpleName().toString();
        String methodName = capitalizeFieldName(fieldName);

        String methodCode = String.format("public %s %s (){return this.%s}", element.asType(), methodName, fieldName);

    }

    private String capitalizeFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
