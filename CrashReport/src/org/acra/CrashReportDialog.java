package org.acra;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
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
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        LinearLayout root = new LinearLayout(this);
        TextView text = new TextView(this);
        text.setText("Bouh!");
        root.addView(text);
        LinearLayout buttons = new LinearLayout(this);
        Button yes = new Button(this);
        yes.setText(android.R.string.yes);
        buttons.addView(yes);
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ErrorReporter err = ErrorReporter.getInstance();
                err.new ReportsSenderWorker().start();
                Toast.makeText(getApplicationContext(), "Thank you !", Toast.LENGTH_LONG).show();
                finish();
            }

        });
        Button no = new Button(this);
        no.setText(android.R.string.no);
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
        buttons.addView(no);
        root.addView(buttons);
        setContentView(root);

    }

}
