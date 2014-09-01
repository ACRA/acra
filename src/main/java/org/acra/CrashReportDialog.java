/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acra;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.acra.collector.CrashReportData;
import org.acra.util.ToastSender;

import java.io.IOException;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:theme="@android:style/Theme.Dialog" and
 * android:launchMode="singleInstance" in your AndroidManifest to work properly.
 * 
 * @author Kevin Gaudin
 */
public final class CrashReportDialog extends Activity {

    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private SharedPreferences prefs;
    private EditText userComment;
    private EditText userEmail;
    String mReportFileName;
    private View.OnClickListener sendReport = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Retrieve user comment
            final String comment = userComment != null ? userComment.getText().toString() : "";

            // Store the user email
            final String usrEmail;
            if(prefs != null && userEmail != null) {
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
            } catch(IOException e) {
                Log.w(LOG_TAG, "User comment not added: ", e);
            }

            // Start the report sending task
            Log.v(ACRA.LOG_TAG, "About to start SenderWorker from CrashReportDialog");
            ACRA.getErrorReporter().startSendingReports(false, true);

            // Optional Toast to thank the user
            final int toastId = ACRA.getConfig().resDialogOkToast();
            if(toastId != 0) {
                ToastSender.sendToast(getApplicationContext(), toastId, Toast.LENGTH_LONG);
            }
            finish();
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReportFileName = getIntent().getStringExtra(ACRAConstants.EXTRA_REPORT_FILE_NAME);
        Log.d(LOG_TAG, "Opening CrashReportDialog for " + mReportFileName);
        if (mReportFileName == null) {
            finish();
        }
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        if(ACRA.getConfig().resDialogLayout() != ACRAConstants.DEFAULT_RES_VALUE) {
            setContentView(ACRA.getConfig().resDialogLayout());
            if(findViewById(android.R.id.button1) != null) {
                findViewById(android.R.id.button1).setOnClickListener(sendReport);
            }
        } else {
            final LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(10, 10, 10, 10);
            root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            root.setFocusable(true);
            root.setFocusableInTouchMode(true);

            final ScrollView scroll = new ScrollView(this);
            root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            final LinearLayout scrollable = new LinearLayout(this);
            scrollable.setOrientation(LinearLayout.VERTICAL);
            scroll.addView(scrollable);

            final TextView text = new TextView(this);
            text.setText(getText(ACRA.getConfig().resDialogText()));
            scrollable.addView(text);

            // Add an optional prompt for user comments
            final int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
            if(commentPromptId != 0) {
                final TextView label = new TextView(this);
                label.setText(getText(commentPromptId));

                label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
                scrollable.addView(label,
                        new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                userComment = new EditText(this);
                userComment.setLines(2);
                if(savedInstanceState != null) {
                    String savedValue = savedInstanceState.getString(STATE_COMMENT);
                    if(savedValue != null) {
                        userComment.setText(savedValue);
                    }
                }
                scrollable.addView(userComment);
            }

            // Add an optional user email field
            final int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
            if(emailPromptId != 0) {
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
                if(savedInstanceState != null) {
                    savedValue = savedInstanceState.getString(STATE_EMAIL);
                }
                if(savedValue != null) {
                    userEmail.setText(savedValue);
                } else {
                    userEmail.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));
                }

                scrollable.addView(userEmail);
            }

            final LinearLayout buttons = new LinearLayout(this);
            buttons.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            buttons.setPadding(buttons.getPaddingLeft(), 10, buttons.getPaddingRight(), buttons.getPaddingBottom());

            final Button yes = new Button(this);
            yes.setText(android.R.string.yes);
            yes.setOnClickListener(sendReport);
            buttons.addView(yes, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
            final Button no = new Button(this);
            no.setText(android.R.string.no);
            no.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Let's delete all non approved reports. We keep approved and
                    // silent reports.
                    ACRA.getErrorReporter().deletePendingNonApprovedReports(false);
                    finish();
                }

            });
            buttons.addView(no, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
            root.addView(buttons, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            setContentView(root);

            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.scrollTo(0, 0);
                }
            });
        }

        final int resTitle = ACRA.getConfig().resDialogTitle();
        if (resTitle != 0) {
            setTitle(resTitle);
        }

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, ACRA.getConfig().resDialogIcon());

        cancelNotification();
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

    /**
     * Disable the notification in the Status Bar.
     */
    protected void cancelNotification() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(ACRAConstants.NOTIF_CRASH_ID);
    }
    
    @Override 
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If BACK button is pressed, handle it like a NO.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ACRA.getErrorReporter().deletePendingNonApprovedReports(false);
        }
        return super.onKeyUp(keyCode, event);
    }
}
