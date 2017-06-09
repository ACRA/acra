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

package org.acra.annotation;

import android.app.Application;
import android.support.annotation.NonNull;

import org.acra.ReportField;
import org.acra.collections.ImmutableList;
import org.acra.collections.ImmutableMap;
import org.acra.collections.ImmutableSet;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigUtils;
import org.acra.config.ConfigurationBuilder;
import org.acra.config.ConfigurationBuilderFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author F43nd1r
 * @since 01.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration(configName = "MailSenderConfiguration", packageName = "org.acra.config")
public @interface AcraEmailSender {

    /**
     * <p>
     * Add your crash reports mailbox here if you want to send reports via
     * email. This allows to get rid of the INTERNET permission. Reports content
     * can be customized with {@link AcraCore#customReportContent()} . Default fields
     * are:
     * </p>
     * <ul>
     * <li>
     * {@link ReportField#USER_COMMENT}</li>
     * <li>
     * {@link ReportField#ANDROID_VERSION}</li>
     * <li>
     * {@link ReportField#APP_VERSION_NAME}</li>
     * <li>
     * {@link ReportField#BRAND}</li>
     * <li>
     * {@link ReportField#PHONE_MODEL}</li>
     * <li>
     * {@link ReportField#CUSTOM_DATA}</li>
     * <li>
     * {@link ReportField#STACK_TRACE}</li>
     * </ul>
     *
     * @return email address to which to send reports.
     */
    @NonNull String mailTo();

    /**
     * @return if the report should be an attachment instead of plain text.
     */
    boolean reportAsFile() default true;
}
