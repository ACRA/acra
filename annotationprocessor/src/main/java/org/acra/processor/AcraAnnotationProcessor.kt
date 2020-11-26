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
package org.acra.processor

import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import org.acra.annotation.Configuration
import org.acra.processor.creator.ClassCreator
import org.acra.processor.creator.ServiceResourceCreator
import org.acra.processor.util.Types
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * @author F43nd1r
 * @since 18.03.2017
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class AcraAnnotationProcessor : AbstractProcessor() {
    private val serviceResourceCreator = ServiceResourceCreator()

    override fun getSupportedAnnotationTypes(): Set<String> {
        return Types.MARKER_ANNOTATIONS.map { it.reflectionName() }.toSet()
    }

    override fun process(annotations: Set<TypeElement?>?, roundEnv: RoundEnvironment): Boolean {
        try {
            for (e in roundEnv.getElementsAnnotatedWith(Configuration::class.java)) {
                if (e.kind == ElementKind.ANNOTATION_TYPE) {
                    ClassCreator(MoreElements.asType(e), e.getAnnotation(Configuration::class.java), processingEnv, serviceResourceCreator).createClasses()
                } else {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, String.format("%s is only supported on %s",
                            Configuration::class.java.name, ElementKind.ANNOTATION_TYPE.name), e)
                }
            }
            if(roundEnv.processingOver()) {
                serviceResourceCreator.generateResources(processingEnv)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate acra classes")
        }
        return true
    }
}