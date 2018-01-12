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

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.acra.ACRAConstants;
import org.acra.sender.EmailIntentSender;

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
@Configuration
public @interface AcraMailSender {

    /**
     * your crash reports mailbox
     *
     * @return email address to which to send reports.
     * @since 5.0.0
     */
    @NonNull String mailTo();

    /**
     * Sending the report as an attachment prevents issues with report size and the user from modifying it
     *
     * @return if the report should be an attachment instead of plain text.
     * @since 5.0.0
     */
    boolean reportAsFile() default true;

    /**
     * custom file name for the report
     *
     * @return report file name
     * @since 5.0.1
     */
    @NonNull String reportFileName() default EmailIntentSender.DEFAULT_REPORT_FILENAME;

    /**
     * custom email subject.
     * Default is "&lt;applicationId&gt; Crash Report"
     *
     * @return resource id of the custom email subject
     * @since 5.0.1
     */
    @StringRes int resSubject() default ACRAConstants.DEFAULT_RES_VALUE;
}
