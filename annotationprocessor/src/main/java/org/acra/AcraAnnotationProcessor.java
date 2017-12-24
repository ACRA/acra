/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra;

import com.google.auto.service.AutoService;

import org.acra.annotation.AnyNonDefault;
import org.acra.annotation.Configuration;
import org.acra.annotation.Instantiatable;
import org.acra.annotation.NonEmpty;
import org.acra.annotation.PreBuild;
import org.acra.annotation.Transform;
import org.acra.creator.ClassCreator;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * @author F43nd1r
 * @since 18.03.2017
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AcraAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(AnyNonDefault.class, Configuration.class, Instantiatable.class, NonEmpty.class, PreBuild.class, Transform.class)
                .map(Class::getName).collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final ArrayList<? extends Element> annotatedElements = new ArrayList<>(roundEnv.getElementsAnnotatedWith(Configuration.class));
            if (!annotatedElements.isEmpty()) {
                for (final Element e : annotatedElements) {
                    if (e.getKind() == ElementKind.ANNOTATION_TYPE) {
                        new ClassCreator((TypeElement) e, e.getAnnotation(Configuration.class), new ModelUtils(processingEnv)).createClasses();
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("%s is only supported on %s",
                                Configuration.class.getName(), ElementKind.ANNOTATION_TYPE.name()), e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate acra classes");
        }
        return true;
    }

}
