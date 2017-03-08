package org.acra;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

import java.io.IOException;

import org.acra.collector.CrashReportData;
import org.acra.util.ToastSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:launchMode="singleInstance" in your
 * AndroidManifest to work properly.
 **/
public class CrashReportDialog extends Activity implements DialogInterface.OnClickListener, OnDismissListener {
    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private SharedPreferences prefs;
    private EditText userComment;
    private EditText userEmail;
    String mReportFileName;
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean forceCancel = getIntent().getBooleanExtra(ACRAConstants.EXTRA_FORCE_CANCEL, false);
        if(forceCancel) {
            ACRA.log.d(ACRA.LOG_TAG, "Forced reports deletion.");
            cancelReports();
            finish();
            return;
        }

        mReportFileName = getIntent().getStringExtra(ACRAConstants.EXTRA_REPORT_FILE_NAME);
        Log.d(LOG_TAG, "Opening CrashReportDialog for " + mReportFileName);
        if (mReportFileName == null) {
            finish();
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        int resourceId = ACRA.getConfig().resDialogTitle();
        if(resourceId != 0) {
            dialogBuilder.setTitle(resourceId);
        }
        resourceId = ACRA.getConfig().resDialogIcon();
        if(resourceId != 0) {
            dialogBuilder.setIcon(resourceId);
        }
        dialogBuilder.setView(buildCustomView(savedInstanceState));
        dialogBuilder.setPositiveButton(android.R.string.ok, CrashReportDialog.this);
        dialogBuilder.setNegativeButton(android.R.string.cancel, CrashReportDialog.this);
        cancelNotification();
        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private View buildCustomView(Bundle savedInstanceState) {
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

    /**
     * Disable the notification in the Status Bar.
     */
    protected void cancelNotification() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(ACRAConstants.NOTIF_CRASH_ID);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            sendCrash();
        else {
            cancelReports();
        }
        finish();
    }

    private void cancelReports() {
        ACRA.getErrorReporter().deletePendingNonApprovedReports(false);
    }

    private void sendCrash() {
        // Retrieve user comment
        final String comment = userComment != null ? userComment.getText().toString() : "";

        // Store the user email
        final String usrEmail;
        if (prefs != null && userEmail != null) {
            usrEmail = userEmail.getText().toString();
            final Editor prefEditor = prefs.edit();
            prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, usrEmail);
            prefEditor.commit();
        } else {
            usrEmail = "";
        }

        final CrashReportPersister persister = new CrashReportPersister(getApplicationContext());
        try {
            Log.d(LOG_TAG, "Add user comment to " + mReportFileName);
            final CrashReportData crashData = persister.load(mReportFileName);
            crashData.put(USER_COMMENT, comment);
            crashData.put(USER_EMAIL, usrEmail);
            persister.store(crashData, mReportFileName);
        } catch (IOException e) {
            Log.w(LOG_TAG, "User comment not added: ", e);
        }

        // Start the report sending task
        Log.v(ACRA.LOG_TAG, "About to start SenderWorker from CrashReportDialog");
        ACRA.getErrorReporter().startSendingReports(false, true);

        // Optional Toast to thank the user
        final int toastId = ACRA.getConfig().resDialogOkToast();
        if (toastId != 0) {
            ToastSender.sendToast(getApplicationContext(), toastId, Toast.LENGTH_LONG);
        }
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}