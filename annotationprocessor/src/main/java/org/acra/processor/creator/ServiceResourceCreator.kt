package org.acra.processor.creator

import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.StandardLocation

class ServiceResourceCreator {
    private val services = mutableMapOf<String, MutableSet<String>>()

    fun addService(interfaceName: String, className: String) {
        (services[interfaceName] ?: mutableSetOf<String>().also { services[interfaceName] = it }).add(className)
    }

    fun generateResources(processingEnv: ProcessingEnvironment) {
        services.forEach { (interfaceName, services) ->
            val resourceFile = "META-INF/services/${interfaceName}"
            val oldContent: Set<String> = try {
                processingEnv.filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceFile).openInputStream().bufferedReader().use {
                    it.readLines().toSet()
                }
            } catch (e: IOException) {
                emptySet()
            }
            val content: Set<String> = oldContent + services
            if (content.size > oldContent.size) {
                processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile)
                        .openOutputStream().bufferedWriter().use {
                            it.write(content.joinToString("\n"))
                        }
            }
        }
    }
}