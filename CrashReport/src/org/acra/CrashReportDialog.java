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
import android.widget.Toast;

public class CrashReportDialog extends Activity {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        
        TextView text = new TextView(this);
        text.setText("Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! \nBouh! Bouh! Bouh! \nBouh! Bouh! Bouh! Bouh! \n\nBouh! Bouh! Bouh!Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! \nBouh! Bouh! Bouh! \nBouh! Bouh! Bouh! Bouh! \n\nBouh! Bouh! Bouh!Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! \nBouh! Bouh! Bouh! \nBouh! Bouh! Bouh! Bouh! \n\nBouh! Bouh! Bouh!Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! Bouh! \nBouh! Bouh! Bouh! \nBouh! Bouh! Bouh! Bouh! \n\nBouh! Bouh! Bouh!");
        scroll.addView(text, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        
        EditText userComment = new EditText(this);
        userComment.setLines(3);
        userComment.setText("User comment");
        root.addView(userComment, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        Button yes = new Button(this);
        yes.setText(android.R.string.yes);
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter err = ErrorReporter.getInstance();
                err.new ReportsSenderWorker().start();
                Toast.makeText(getApplicationContext(), "Thank you !", Toast.LENGTH_LONG).show();
                finish();
            }

        });
        buttons.addView(yes, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        Button no = new Button(this);
        no.setText(android.R.string.no);
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
        buttons.addView(no, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        root.addView(buttons, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        setContentView(root);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                android.R.drawable.ic_dialog_alert);
        cancelNotification();
    }

    protected void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(CrashReportingApplication.NOTIF_CRASH_ID);
    }

}
