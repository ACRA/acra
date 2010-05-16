package org.acra;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrashReportDialog extends Activity {

    private static final int CRASH_DIALOG_LEFT_ICON = android.R.drawable.ic_dialog_alert;
    private EditText userComment = null;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        Bundle crashResources = ((CrashReportingApplication) getApplication())
                .getCrashResources();
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

        TextView text = new TextView(this);

        text.setText(getText(crashResources
                .getInt(CrashReportingApplication.RES_DIALOG_TEXT)));
        scroll
                .addView(text, LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT);

        // Add an optional prompt for user comments
        int commentPromptId = crashResources
                .getInt(CrashReportingApplication.RES_DIALOG_COMMENT_PROMPT);
        if (commentPromptId != 0) {
            TextView label = new TextView(this);
            label.setText(getText(commentPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label
                    .getPaddingRight(), label.getPaddingBottom());
            root.addView(label, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            userComment = new EditText(this);

            userComment.setLines(3);
            // userComment.setText("User comment");
            root.addView(userComment, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        buttons.setPadding(buttons.getPaddingLeft(), 10, buttons
                .getPaddingRight(), buttons.getPaddingBottom());

        Button yes = new Button(this);
        yes.setText(android.R.string.yes);
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter err = ErrorReporter.getInstance();
                if (userComment != null) {
                    err.addCustomData(ErrorReporter.USER_COMMENT_KEY,
                            userComment.getText().toString());
                }
                err.new ReportsSenderWorker().start();
                // Toast.makeText(getApplicationContext(), "Thank you !",
                // Toast.LENGTH_LONG).show();
                finish();
            }

        });
        buttons.addView(yes, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        Button no = new Button(this);
        no.setText(android.R.string.no);
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter.getInstance().deletePendingReports();
                finish();
            }

        });
        buttons.addView(no, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        root.addView(buttons, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        setContentView(root);

        if(crashResources.containsKey(CrashReportingApplication.RES_DIALOG_TITLE)) {
            setTitle(crashResources.getInt(CrashReportingApplication.RES_DIALOG_TITLE));
        }

        
        if (crashResources
                .containsKey(CrashReportingApplication.RES_DIALOG_ICON)) {
            getWindow().setFeatureDrawableResource(
                    Window.FEATURE_LEFT_ICON,
                    crashResources
                            .getInt(CrashReportingApplication.RES_DIALOG_ICON));
        } else {
            getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    CRASH_DIALOG_LEFT_ICON);
        }

        cancelNotification();
    }

    protected void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(CrashReportingApplication.NOTIF_CRASH_ID);
    }

}
