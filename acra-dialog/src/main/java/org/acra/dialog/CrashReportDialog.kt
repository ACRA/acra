/*
 * Copyright (c) 2017 the ACRA team
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
package org.acra.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.CallSuper
import org.acra.ACRA
import org.acra.ACRAConstants
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.DialogConfiguration
import org.acra.prefs.SharedPreferencesFactory

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports.
 *
 * @author F43nd1r &amp; Various
 */
@Suppress("MemberVisibilityCanBePrivate")
open class CrashReportDialog : Activity(), DialogInterface.OnClickListener {
    private lateinit var scrollable: LinearLayout
    private var userCommentView: EditText? = null
    private var userEmailView: EditText? = null
    private lateinit var sharedPreferencesFactory: SharedPreferencesFactory
    private lateinit var dialogConfiguration: DialogConfiguration
    private lateinit var helper: CrashReportDialogHelper
    private var padding = 0

    /**
     * @return the AlertDialog displayed by this Activity
     */
    protected lateinit var dialog: AlertDialog
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            helper = CrashReportDialogHelper(this, intent)
            scrollable = LinearLayout(this)
            scrollable.orientation = LinearLayout.VERTICAL
            sharedPreferencesFactory = SharedPreferencesFactory(applicationContext, helper.config)
            dialogConfiguration = getPluginConfiguration(helper.config, DialogConfiguration::class.java)
            dialogConfiguration.resTheme.takeIf { it != ACRAConstants.DEFAULT_RES_VALUE }?.let { setTheme(it) }
            padding = loadPaddingFromTheme()
            buildAndShowDialog(savedInstanceState)
        } catch (e: IllegalArgumentException) {
            finish()
        }
    }

    /**
     * Build the dialog from the values in config
     *
     * @param savedInstanceState old state to restore
     */
    protected fun buildAndShowDialog(savedInstanceState: Bundle?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogConfiguration.title.takeIf { it.isNotEmpty() }?.let { dialogBuilder.setTitle(title) }
        dialogConfiguration.resIcon.takeIf { it != ACRAConstants.DEFAULT_RES_VALUE }?.let { dialogBuilder.setIcon(it) }
        dialogBuilder.setView(buildCustomView(savedInstanceState))
                .setPositiveButton(dialogConfiguration.positiveButtonText, this)
                .setNegativeButton(dialogConfiguration.negativeButtonText, this)
        dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener {
            finish()
        }
        dialog.show()
    }

    protected fun buildCustomView(savedInstanceState: Bundle?): View {
        val root = ScrollView(this)
                .apply {
                    setPadding(padding, padding, padding, padding)
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    isFocusable = true
                    isFocusableInTouchMode = true
                    addView(scrollable)
                }
        addViewToDialog(getMainView())

        // Add an optional prompt for user comments
        getCommentLabel()?.let {
            it.setPadding(it.paddingLeft, padding, it.paddingRight, it.paddingBottom)
            addViewToDialog(it)
            var savedComment: String? = null
            if (savedInstanceState != null) {
                savedComment = savedInstanceState.getString(STATE_COMMENT)
            }
            userCommentView = getCommentPrompt(savedComment).apply { addViewToDialog(this) }
        }

        // Add an optional user email field
        getEmailLabel()?.let {
            it.setPadding(it.paddingLeft, padding, it.paddingRight, it.paddingBottom)
            addViewToDialog(it)
            var savedEmail: String? = null
            if (savedInstanceState != null) {
                savedEmail = savedInstanceState.getString(STATE_EMAIL)
            }
            userEmailView = getEmailPrompt(savedEmail).apply { addViewToDialog(this) }
        }
        return root
    }

    /**
     * adds a view to the end of the dialog
     *
     * @param v the view to add
     */
    protected fun addViewToDialog(v: View) {
        scrollable.addView(v)
    }

    /**
     * Creates a main view containing text of resText, or nothing if not found
     *
     * @return the main view
     */
    protected fun getMainView(): View {
        return TextView(this).apply { dialogConfiguration.text.takeIf { it.isNotEmpty() }?.let { text = it } }
    }

    /**
     * creates a comment label view with resCommentPrompt as text
     *
     * @return the label or null if there is no resource
     */
    protected fun getCommentLabel(): View? {
        return dialogConfiguration.commentPrompt.takeIf { it.isNotEmpty() }?.let { TextView(this).apply { text = it } }
    }

    /**
     * creates a comment prompt
     *
     * @param savedComment the content of the prompt (usually from a saved state)
     * @return the comment prompt
     */
    protected fun getCommentPrompt(savedComment: CharSequence?): EditText {
        return EditText(this).apply {
            setLines(2)
            savedComment?.let { setText(it) }
        }
    }

    /**
     * creates a email label view with resEmailPrompt as text
     *
     * @return the label or null if there is no resource
     */
    protected fun getEmailLabel(): View? {
        return dialogConfiguration.emailPrompt.takeIf { it.isNotEmpty() }?.let { TextView(this).apply { text = it } }
    }

    /**
     * creates an email prompt
     *
     * @param savedEmail the content of the prompt (usually from a saved state or settings)
     * @return the email prompt
     */
    protected fun getEmailPrompt(savedEmail: CharSequence?): EditText {
        return EditText(this).apply {
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(savedEmail ?: sharedPreferencesFactory.create().getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""))
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            val comment = userCommentView?.text?.toString() ?: ""

            // Store the user email
            val prefs = sharedPreferencesFactory.create()
            val userEmail: String = userEmailView?.text?.toString()?.also { prefs.edit().putString(ACRA.PREF_USER_EMAIL_ADDRESS, it).apply() } ?: prefs.getString(
                    ACRA.PREF_USER_EMAIL_ADDRESS, "")!!
            helper.sendCrash(comment, userEmail)
        } else {
            helper.cancelReports()
        }
        finish()
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        userCommentView?.text?.let { outState.putString(STATE_COMMENT, it.toString()) }
        userEmailView?.text?.let { outState.putString(STATE_EMAIL, it.toString()) }
    }

    /**
     * @return value of ?dialogPreferredPadding from theme or 10 if not set.
     */
    protected fun loadPaddingFromTheme(): Int {
        val value = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.dialogPreferredPadding, value, true)) {
            TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
        } else 10 //attribute not set, fall back to a default value
    }

    companion object {
        private const val STATE_EMAIL = "email"
        private const val STATE_COMMENT = "comment"
    }
}