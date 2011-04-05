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

import static org.acra.ACRA.LOG_TAG;

import org.acra.ErrorReporter.ReportsSenderWorker;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:theme="@android:style/Theme.Dialog" and
 * android:launchMode="singleInstance" in your AndroidManifest to work properly.
 * 
 * @author Kevin Gaudin
 * 
 */
public class CrashReportDialog extends Activity {

    private SharedPreferences prefs = null;
    private EditText userComment = null;
    private EditText userEmail = null;
    String mReportFileName = null;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReportFileName = getIntent().getStringExtra(ErrorReporter.EXTRA_REPORT_FILE_NAME);
        Log.d(LOG_TAG, "Opening CrashReportDialog for " + mReportFileName);
        if (mReportFileName == null) {
            finish();
        }
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

        TextView text = new TextView(this);

        text.setText(getText(ACRA.getConfig().resDialogText()));
        scroll.addView(text, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        // Add an optional prompt for user comments
        int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
        if (commentPromptId != 0) {
            TextView label = new TextView(this);
            label.setText(getText(commentPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            root.addView(label, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            userComment = new EditText(this);

            userComment.setLines(2);
            // userComment.setText("User comment");
            root.addView(userComment,
                    new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        // Add an optional user email field
        int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
        if (emailPromptId != 0) {
            TextView label = new TextView(this);
            label.setText(getText(emailPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            root.addView(label, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            userEmail = new EditText(this);
            userEmail.setSingleLine();
            userEmail.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            prefs = getSharedPreferences(ACRA.getConfig().sharedPreferencesName(), ACRA.getConfig()
                    .sharedPreferencesMode());
            userEmail.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));

            root.addView(userEmail, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        buttons.setPadding(buttons.getPaddingLeft(), 10, buttons.getPaddingRight(), buttons.getPaddingBottom());

        Button yes = new Button(this);
        yes.setText(android.R.string.yes);
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter err = ErrorReporter.getInstance();

                // Start the report sending task
                ReportsSenderWorker worker = err.new ReportsSenderWorker();
                worker.setApprovePendingReports();

                // Retrieve user comment
                if(userComment != null) {
                    worker.setUserComment(mReportFileName, userComment.getText().toString());
                }
                
                // Store the user email
                if (prefs != null && userEmail != null) {
                    String usrEmail = userEmail.getText().toString();
                    Editor prefEditor = prefs.edit();
                    prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, usrEmail);
                    prefEditor.commit();
                    worker.setUserEmail(mReportFileName, usrEmail);
                }

                worker.start();


                // Optional Toast to thank the user
                int toastId = ACRA.getConfig().resDialogOkToast();
                if (toastId != 0) {
                    Toast.makeText(getApplicationContext(), toastId, Toast.LENGTH_LONG).show();
                }
                finish();
            }

        });
        buttons.addView(yes, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        Button no = new Button(this);
        no.setText(android.R.string.no);
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter.getInstance().deletePendingReports();
                finish();
            }

        });
        buttons.addView(no, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        root.addView(buttons, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        setContentView(root);

        int resTitle = ACRA.getConfig().resDialogTitle();
        if (resTitle != 0) {
            setTitle(resTitle);
        }

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, ACRA.getConfig().resDialogIcon());

        cancelNotification();
    }

    /**
     * Disable the notification in the Status Bar.
     */
    protected void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(ACRA.NOTIF_CRASH_ID);
    }

}
