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

import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import org.acra.ACRAConstants;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CrashReportDialog configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration
public @interface AcraDialog {

    /**
     * Custom CrashReportDialog class
     *
     * @return Class for the CrashReportDialog used when prompting the user for crash details.
     * @since 5.0.0
     */
    @Instantiatable @AnyNonDefault @NonNull Class<? extends BaseCrashReportDialog> reportDialogClass() default CrashReportDialog.class;

    /**
     * label of the positive button
     *
     * @return Resource id for the positive button label in the crash dialog.
     * @see android.app.AlertDialog.Builder#setPositiveButton(int, DialogInterface.OnClickListener)
     * @since 5.0.0
     */
    @StringRes int resPositiveButtonText() default android.R.string.ok;

    /**
     * label of the negative button
     *
     * @return Resource id for the negative button label in the crash dialog.
     * @see android.app.AlertDialog.Builder#setNegativeButton(int, DialogInterface.OnClickListener)
     * @since 5.0.0
     */
    @StringRes int resNegativeButtonText() default android.R.string.cancel;

    /**
     * label of the comment input prompt.
     * If not provided, removes the input field.
     *
     * @return Resource id for the user comment input label in the crash dialog.
     * @since 5.0.0
     */
    @StringRes int resCommentPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * label of the email input prompt.
     * If not provided, removes the input field.
     *
     * @return Resource id for the user email address input label in the crash dialog.
     * @since 5.0.0
     */
    @StringRes int resEmailPrompt() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * icon of the dialog
     *
     * @return Resource id for the icon in the crash dialog.
     * @see android.app.AlertDialog.Builder#setIcon(int)
     * @since 5.0.0
     */
    @DrawableRes int resIcon() default android.R.drawable.ic_dialog_alert;

    /**
     * text in the dialog
     *
     * @return Resource id for the text in the crash dialog.
     * @since 5.0.0
     */
    @AnyNonDefault @StringRes int resText() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * title of the dialog
     *
     * @return Resource id for the title in the crash dialog.
     * @see android.app.AlertDialog.Builder#setTitle(int)
     * @since 5.0.0
     */
    @StringRes int resTitle() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * theme of the dialog
     *
     * @return resource id for the crash dialog theme
     * @see android.app.Activity#setTheme(int)
     * @since 5.0.0
     */
    @StyleRes int resTheme() default ACRAConstants.DEFAULT_RES_VALUE;
}
