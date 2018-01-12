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

package org.acra.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.ConfigUtils;
import org.acra.config.DialogConfiguration;
import org.acra.prefs.SharedPreferencesFactory;


/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports.
 *
 * @author F43nd1r &amp; Various
 **/
@SuppressWarnings({"WeakerAccess", "unused"})
public class CrashReportDialog extends BaseCrashReportDialog implements DialogInterface.OnClickListener {

    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private static final int PADDING = 10;

    private LinearLayout scrollable;
    private EditText userCommentView;
    private EditText userEmailView;
    private SharedPreferencesFactory sharedPreferencesFactory;
    private DialogConfiguration dialogConfiguration;

    private AlertDialog mDialog;

    @CallSuper
    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        sharedPreferencesFactory = new SharedPreferencesFactory(getApplicationContext(), getConfig());
        dialogConfiguration = ConfigUtils.getPluginConfiguration(getConfig(), DialogConfiguration.class);
        final int themeResourceId = dialogConfiguration.resTheme();
        if (themeResourceId != ACRAConstants.DEFAULT_RES_VALUE) setTheme(themeResourceId);

        buildAndShowDialog(savedInstanceState);
    }

    /**
     * Build the dialog from the values in config
     *
     * @param savedInstanceState old state to restore
     */
    protected void buildAndShowDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final String title = dialogConfiguration.title();
        if (title != null) {
            dialogBuilder.setTitle(title);
        }
        final int iconResourceId = dialogConfiguration.resIcon();
        if (iconResourceId != ACRAConstants.DEFAULT_RES_VALUE) {
            dialogBuilder.setIcon(iconResourceId);
        }
        dialogBuilder.setView(buildCustomView(savedInstanceState))
                .setPositiveButton(dialogConfiguration.positiveButtonText(), this)
                .setNegativeButton(dialogConfiguration.negativeButtonText(), this);

        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @NonNull
    protected View buildCustomView(@Nullable Bundle savedInstanceState) {
        final ScrollView root = new ScrollView(this);
        root.setPadding(PADDING, PADDING, PADDING, PADDING);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        root.addView(scrollable);

        addViewToDialog(getMainView());

        // Add an optional prompt for user comments
        final View comment = getCommentLabel();
        if (comment != null) {
            comment.setPadding(comment.getPaddingLeft(), PADDING, comment.getPaddingRight(), comment.getPaddingBottom());
            addViewToDialog(comment);
            String savedComment = null;
            if (savedInstanceState != null) {
                savedComment = savedInstanceState.getString(STATE_COMMENT);
            }
            userCommentView = getCommentPrompt(savedComment);
            addViewToDialog(userCommentView);
        }

        // Add an optional user email field
        final View email = getEmailLabel();
        if (email != null) {
            email.setPadding(email.getPaddingLeft(), PADDING, email.getPaddingRight(), email.getPaddingBottom());
            addViewToDialog(email);
            String savedEmail = null;
            if (savedInstanceState != null) {
                savedEmail = savedInstanceState.getString(STATE_EMAIL);
            }
            userEmailView = getEmailPrompt(savedEmail);
            addViewToDialog(userEmailView);
        }
        return root;
    }

    /**
     * adds a view to the end of the dialog
     *
     * @param v the view to add
     */
    protected final void addViewToDialog(@NonNull View v) {
        scrollable.addView(v);
    }

    /**
     * Creates a main view containing text of resText, or nothing if not found
     *
     * @return the main view
     */
    @NonNull
    protected View getMainView() {
        final TextView text = new TextView(this);
        final String dialogText = dialogConfiguration.text();
        if (dialogText != null) {
            text.setText(dialogText);
        }
        return text;
    }

    /**
     * creates a comment label view with resCommentPrompt as text
     *
     * @return the label or null if there is no resource
     */
    @Nullable
    protected View getCommentLabel() {
        final String commentPrompt = dialogConfiguration.commentPrompt();
        if (commentPrompt != null) {
            final TextView labelView = new TextView(this);
            labelView.setText(commentPrompt);
            return labelView;
        }
        return null;
    }

    /**
     * creates a comment prompt
     *
     * @param savedComment the content of the prompt (usually from a saved state)
     * @return the comment prompt
     */
    @NonNull
    protected EditText getCommentPrompt(@Nullable CharSequence savedComment) {
        final EditText userCommentView = new EditText(this);
        userCommentView.setLines(2);
        if (savedComment != null) {
            userCommentView.setText(savedComment);
        }
        return userCommentView;
    }

    /**
     * creates a email label view with resEmailPrompt as text
     *
     * @return the label or null if there is no resource
     */
    @Nullable
    protected View getEmailLabel() {
        final String emailPrompt = dialogConfiguration.emailPrompt();
        if (emailPrompt != null) {
            final TextView labelView = new TextView(this);
            labelView.setText(emailPrompt);
            return labelView;
        }
        return null;
    }

    /**
     * creates an email prompt
     *
     * @param savedEmail the content of the prompt (usually from a saved state or settings)
     * @return the email prompt
     */
    @NonNull
    protected EditText getEmailPrompt(@Nullable CharSequence savedEmail) {
        final EditText userEmailView = new EditText(this);
        userEmailView.setSingleLine();
        userEmailView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        if (savedEmail != null) {
            userEmailView.setText(savedEmail);
        } else {
            final SharedPreferences prefs = sharedPreferencesFactory.create();
            userEmailView.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));
        }
        return userEmailView;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            final String comment = userCommentView != null ? userCommentView.getText().toString() : "";

            // Store the user email
            final String userEmail;
            final SharedPreferences prefs = sharedPreferencesFactory.create();
            if (userEmailView != null) {
                userEmail = userEmailView.getText().toString();
                prefs.edit().putString(ACRA.PREF_USER_EMAIL_ADDRESS, userEmail).apply();
            } else {
                userEmail = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "");
            }
            sendCrash(comment, userEmail);
        } else {
            cancelReports();
        }

        finish();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @CallSuper
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userCommentView != null && userCommentView.getText() != null) {
            outState.putString(STATE_COMMENT, userCommentView.getText().toString());
        }
        if (userEmailView != null && userEmailView.getText() != null) {
            outState.putString(STATE_EMAIL, userEmailView.getText().toString());
        }
    }

    /**
     * @return the AlertDialog displayed by this Activity
     */
    protected AlertDialog getDialog() {
        return mDialog;
    }
}