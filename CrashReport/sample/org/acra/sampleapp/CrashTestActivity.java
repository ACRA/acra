package org.acra.sampleapp;

import android.app.Activity;
import android.os.Bundle;

public class CrashTestActivity extends Activity {

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Object nul = null;
        nul.toString();
    }
}
