package org.acra.sampleapp;

import org.acra.ErrorReporter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CrashTestLauncher extends Activity {

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_test_launcher);
        Button btn = (Button) findViewById(R.id.btn_crash);
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CrashTestActivity.class);
                startActivity(intent);
            }
        });
        
        btn = (Button) findViewById(R.id.btn_silent_report);
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ErrorReporter.getInstance().handleSilentException(new Exception("This is a silent report!"));
            }
        });

        btn = (Button) findViewById(R.id.btn_prefs);
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Preferences.class));
            }
        });

    }

}
