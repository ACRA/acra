/*
 * Copyright (c) 2021
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

package org.acra.config

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.faendir.kotlin.autodsl.AutoDsl
import com.faendir.kotlin.autodsl.AutoDslRequired
import org.acra.annotation.AcraDsl
import org.acra.dialog.CrashReportDialog
import org.acra.ktx.plus

/**
 * CrashReportDialog configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@AutoDsl(dslMarker = AcraDsl::class)
class DialogConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,

    /**
     * Custom CrashReportDialog class
     *
     * Inside the activity, use [org.acra.dialog.CrashReportDialogHelper] to integrate it ACRA.
     *
     * @since 5.0.0
     */
    @AutoDslRequired("main")
    val reportDialogClass: Class<out Activity> = CrashReportDialog::class.java,

    /**
     * label of the positive button
     *
     * Defaults to [android.R.string.ok]
     * @see android.app.AlertDialog.Builder.setPositiveButton
     * @since 5.0.0
     */
    val positiveButtonText: String? = null,

    /**
     * label of the negative button
     *
     * Defaults to [android.R.string.cancel]
     * @see android.app.AlertDialog.Builder.setNegativeButton
     * @since 5.0.0
     */
    val negativeButtonText: String? = null,

    /**
     * label of the comment input prompt.
     * If not provided, removes the input field.
     * @since 5.0.0
     */
    val commentPrompt: String? = null,

    /**
     * label of the email input prompt.
     * If not provided, removes the input field.
     * @since 5.0.0
     */
    val emailPrompt: String? = null,

    /**
     * icon of the dialog
     *
     * @see android.app.AlertDialog.Builder.setIcon
     * @since 5.0.0
     */
    @DrawableRes
    val resIcon: Int = android.R.drawable.ic_dialog_alert,

    /**
     * text in the dialog
     *
     * @since 5.0.0
     */
    @AutoDslRequired("main")
    val text: String? = null,

    /**
     * title of the dialog
     *
     * @see android.app.AlertDialog.Builder.setTitle
     * @since 5.0.0
     */
    val title: String? = null,

    /**
     * theme of the dialog
     *
     * @see android.app.Activity.setTheme
     * @since 5.0.0
     */
    @StyleRes
    val resTheme: Int? = null,
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.dialog(initializer: DialogConfigurationBuilder.() -> Unit) {
    pluginConfigurations += DialogConfigurationBuilder().apply(initializer).build()
}
