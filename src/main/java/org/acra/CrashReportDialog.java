package org.acra;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:launchMode="singleInstance" in your
 * AndroidManifest to work properly.
 **/
public class CrashReportDialog extends BaseCrashReportDialog implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private SharedPreferences prefs;
    private EditText userComment;
    private EditText userEmail;

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final int titleResourceId = ACRA.getConfig().resDialogTitle();
        if (titleResourceId != 0) {
            dialogBuilder.setTitle(titleResourceId);
        }
        final int iconResourceId = ACRA.getConfig().resDialogIcon();
        if (iconResourceId != 0) {
            dialogBuilder.setIcon(iconResourceId);
        }
        dialogBuilder.setView(buildCustomView(savedInstanceState));
        dialogBuilder.setPositiveButton(getText(ACRA.getConfig().resDialogPositiveButtonText()), CrashReportDialog.this);
        dialogBuilder.setNegativeButton(getText(ACRA.getConfig().resDialogNegativeButtonText()), CrashReportDialog.this);

        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    protected View buildCustomView(Bundle savedInstanceState) {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        final LinearLayout scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(scrollable);

        final TextView text = new TextView(this);
        final int dialogTextId = ACRA.getConfig().resDialogText();
        if (dialogTextId != 0) {
            text.setText(getText(dialogTextId));
        }
        scrollable.addView(text);

        // Add an optional prompt for user comments
        final int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
        if (commentPromptId != 0) {
            final TextView label = new TextView(this);
            label.setText(getText(commentPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            scrollable.addView(label, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            userComment = new EditText(this);
            userComment.setLines(2);
            if (savedInstanceState != null) {
                String savedValue = savedInstanceState.getString(STATE_COMMENT);
                if (savedValue != null) {
                    userComment.setText(savedValue);
                }
            }
            scrollable.addView(userComment);
        }

        // Add an optional user email field
        final int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
        if (emailPromptId != 0) {
            final TextView label = new TextView(this);
            label.setText(getText(emailPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            scrollable.addView(label);

            userEmail = new EditText(this);
            userEmail.setSingleLine();
            userEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            prefs = getSharedPreferences(ACRA.getConfig().sharedPreferencesName(), ACRA.getConfig()
                    .sharedPreferencesMode());
            String savedValue = null;
            if (savedInstanceState != null) {
                savedValue = savedInstanceState.getString(STATE_EMAIL);
            }
            if (savedValue != null) {
                userEmail.setText(savedValue);
            } else {
                userEmail.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));
            }
            scrollable.addView(userEmail);
        }

        return root;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            final String comment = userComment != null ? userComment.getText().toString() : "";

            // Store the user email
            final String usrEmail;
            if (prefs != null && userEmail != null) {
                usrEmail = userEmail.getText().toString();
                final SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, usrEmail);
                prefEditor.commit();
            } else {
                usrEmail = "";
            }
            sendCrash(comment, usrEmail);
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userComment != null && userComment.getText() != null) {
            outState.putString(STATE_COMMENT, userComment.getText().toString());
        }
        if (userEmail != null && userEmail.getText() != null) {
            outState.putString(STATE_EMAIL, userEmail.getText().toString());
        }
    }
}