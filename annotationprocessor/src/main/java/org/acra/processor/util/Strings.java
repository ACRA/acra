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

package org.acra.processor.util;

import android.support.annotation.NonNull;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * @author F43nd1r
 * @since 08.01.2018
 */

public final class Strings {
    public static final String PREFIX_RES = "res";
    public static final String PREFIX_SETTER = "set";
    public static final String PARAM_0 = "arg0";
    public static final String VAR_ANNOTATION = "annotation";
    public static final String FIELD_DELEGATE = "delegate";
    public static final String FIELD_CONTEXT = "context";
    public static final String FIELD_ENABLED = "enabled";
    public static final String PACKAGE = "org.acra.config";
    public static final String CONTEXT = "android.content.Context";
    public static final String CONFIGURATION_BUILDER_FACTORY = "org.acra.config.ConfigurationBuilderFactory";
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private Strings() {
    }

    public static void addClassJavadoc(@NonNull TypeSpec.Builder builder, @NonNull TypeName base) {
        builder.addJavadoc("Class generated based on {@link $T} ($L)\n", base, DATE_FORMAT.format(Calendar.getInstance().getTime()));
    }

    /**
     * Writes the given class to a respective file in the configuration package
     *
     * @param filer    filer to write to
     * @param typeSpec the class
     * @throws IOException if writing fails
     */
    public static void writeClass(@NonNull Filer filer, @NonNull TypeSpec typeSpec) throws IOException {
        JavaFile.builder(PACKAGE, typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .addFileComment("Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + "\n\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n\n" +
                        "http://www.apache.org/licenses/LICENSE-2.0\n\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.")
                .build()
                .writeTo(filer);
    }
}
