/*
 * Copyright (c) 2018 the ACRA team
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

package org.acra.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import org.acra.annotation.Configuration;
import org.acra.processor.creator.ClassCreator;
import org.acra.processor.util.Types;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author F43nd1r
 * @since 18.03.2017
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AcraAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Types.MARKER_ANNOTATIONS.stream().map(ClassName::reflectionName).collect(Collectors.toSet());
    }

    @Override
    public boolean process(@Nullable Set<? extends TypeElement> annotations, @NonNull RoundEnvironment roundEnv) {
        try {
            final ArrayList<? extends Element> annotatedElements = new ArrayList<>(roundEnv.getElementsAnnotatedWith(Configuration.class));
            if (!annotatedElements.isEmpty()) {
                for (final Element e : annotatedElements) {
                    if (e.getKind() == ElementKind.ANNOTATION_TYPE) {
                        new ClassCreator(MoreElements.asType(e), e.getAnnotation(Configuration.class), processingEnv).createClasses();
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
