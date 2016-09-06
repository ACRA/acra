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
import org.acra.prefs.PrefUtils;
import org.acra.prefs.SharedPreferencesFactory;


/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:launchMode="singleInstance" in your
 * AndroidManifest to work properly.
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

    private AlertDialog mDialog;

    @CallSuper
    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        sharedPreferencesFactory = new SharedPreferencesFactory(getApplicationContext(), getConfig());
        final int themeResourceId = getConfig().resDialogTheme();
        if(themeResourceId != ACRAConstants.DEFAULT_RES_VALUE) setTheme(themeResourceId);

        buildAndShowDialog(savedInstanceState);
    }

    /**
     * Build the dialog from the values in config
     *
     * @param savedInstanceState old state to restore
     */
    protected void buildAndShowDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final int titleResourceId = getConfig().resDialogTitle();
        if (titleResourceId != ACRAConstants.DEFAULT_RES_VALUE) {
            dialogBuilder.setTitle(titleResourceId);
        }
        final int iconResourceId = getConfig().resDialogIcon();
        if (iconResourceId != ACRAConstants.DEFAULT_RES_VALUE) {
            dialogBuilder.setIcon(iconResourceId);
        }
        dialogBuilder.setView(buildCustomView(savedInstanceState))
                .setPositiveButton(getText(getConfig().resDialogPositiveButtonText()), this)
                .setNegativeButton(getText(getConfig().resDialogNegativeButtonText()), this);

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
     * Creates a main view containing text of resDialogText, or nothing if not found
     *
     * @return the main view
     */
    @NonNull
    protected View getMainView() {
        final TextView text = new TextView(this);
        final int dialogTextId = getConfig().resDialogText();
        if (dialogTextId != ACRAConstants.DEFAULT_RES_VALUE) {
            text.setText(getText(dialogTextId));
        }
        return text;
    }

    /**
     * creates a comment label view with resDialogCommentPrompt as text
     *
     * @return the label or null if there is no resource
     */
    @Nullable
    protected View getCommentLabel() {
        final int commentPromptId = getConfig().resDialogCommentPrompt();
        if (commentPromptId != ACRAConstants.DEFAULT_RES_VALUE) {
            final TextView labelView = new TextView(this);
            labelView.setText(getText(commentPromptId));
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
     * creates a email label view with resDialogEmailPrompt as text
     *
     * @return the label or null if there is no resource
     */
    @Nullable
    protected View getEmailLabel() {
        final int emailPromptId = getConfig().resDialogEmailPrompt();
        if (emailPromptId != ACRAConstants.DEFAULT_RES_VALUE) {
            final TextView labelView = new TextView(this);
            labelView.setText(getText(emailPromptId));
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
                final SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, userEmail);
                PrefUtils.save(prefEditor);
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