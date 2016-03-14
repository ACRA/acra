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
import org.acra.config.ACRAConfiguration;
import org.acra.prefs.SharedPreferencesFactory;


/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:launchMode="singleInstance" in your
 * AndroidManifest to work properly.
 **/
public class CrashReportDialog extends BaseCrashReportDialog implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private static final int PADDING = 10;

    private LinearLayout scrollable;
    private EditText userCommentView;
    private EditText userEmailView;
    private ACRAConfiguration config;
    private SharedPreferencesFactory sharedPreferencesFactory;

    private AlertDialog mDialog;

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        config = (ACRAConfiguration) getIntent().getSerializableExtra(ACRAConstants.EXTRA_REPORT_CONFIG);
        sharedPreferencesFactory = new SharedPreferencesFactory(getApplicationContext(), config);

        buildAndShowDialog(savedInstanceState);
    }

    /**
     * Build the dialog from the values in config
     * @param savedInstanceState old state to restore
     */
    protected void buildAndShowDialog(@Nullable Bundle savedInstanceState){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final int titleResourceId = config.resDialogTitle();
        if (titleResourceId != 0) {
            dialogBuilder.setTitle(titleResourceId);
        }
        final int iconResourceId = config.resDialogIcon();
        if (iconResourceId != 0) {
            dialogBuilder.setIcon(iconResourceId);
        }
        dialogBuilder.setView(buildCustomView(savedInstanceState));
        dialogBuilder.setPositiveButton(getText(config.resDialogPositiveButtonText()), CrashReportDialog.this);
        dialogBuilder.setNegativeButton(getText(config.resDialogNegativeButtonText()), CrashReportDialog.this);

        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @NonNull
    protected View buildCustomView(@Nullable Bundle savedInstanceState) {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(PADDING, PADDING, PADDING, PADDING);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        scroll.addView(scrollable);

        addViewToDialog(getMainView());

        // Add an optional prompt for user comments
        final int commentPromptId = config.resDialogCommentPrompt();
        if (commentPromptId != 0) {
            String savedComment = null;
            if (savedInstanceState != null) {
                savedComment = savedInstanceState.getString(STATE_COMMENT);
            }
            userCommentView = getCommentPrompt(getText(commentPromptId), savedComment);
            addViewToDialog(userCommentView);
        }

        // Add an optional user email field
        final int emailPromptId = config.resDialogEmailPrompt();
        if (emailPromptId != 0) {
            String savedEmail = null;
            if (savedInstanceState != null) {
                savedEmail = savedInstanceState.getString(STATE_EMAIL);
            }
            userEmailView = getEmailPrompt(getText(emailPromptId), savedEmail);
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
     * Creates a main view containing text of resDialogText
     *
     * @return the main view
     */
    @NonNull
    protected View getMainView() {
        final TextView text = new TextView(this);
        final int dialogTextId = config.resDialogText();
        if (dialogTextId != 0) {
            text.setText(getText(dialogTextId));
        }
        return text;
    }

    /**
     * creates a comment prompt
     *
     * @param label        the label of the prompt
     * @param savedComment the content of the prompt (usually from a saved state)
     * @return the comment prompt
     */
    @NonNull
    protected EditText getCommentPrompt(CharSequence label, @Nullable CharSequence savedComment) {
        final TextView labelView = new TextView(this);
        labelView.setText(label);

        labelView.setPadding(labelView.getPaddingLeft(), PADDING, labelView.getPaddingRight(), labelView.getPaddingBottom());
        scrollable.addView(labelView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        EditText userCommentView = new EditText(this);
        userCommentView.setLines(2);
        if (savedComment != null) {
            userCommentView.setText(savedComment);
        }
        return userCommentView;
    }

    /**
     * creates an email prompt
     *
     * @param label      the label of the prompt
     * @param savedEmail the content of the prompt (usually from a saved state)
     * @return the email prompt
     */
    @NonNull
    protected EditText getEmailPrompt(CharSequence label, @Nullable CharSequence savedEmail) {
        final TextView labelView = new TextView(this);
        labelView.setText(label);

        labelView.setPadding(labelView.getPaddingLeft(), PADDING, labelView.getPaddingRight(), labelView.getPaddingBottom());
        scrollable.addView(labelView);

        EditText userEmailView = new EditText(this);
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
                prefEditor.commit();
            } else {
                userEmail = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "");
            }
            sendCrash(comment, userEmail);
        } else {
            cancelReports();
        }

        finish();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
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