package org.acra;

import android.app.Application;
import android.net.Uri;

public abstract class CrashReportingApplication extends Application {
    protected static final String LOG_TAG = "CrashReport";

    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //ErrorReporter crashReporter = new ErrorReporter(getFormUri());
        ErrorReporter.getInstance().setFormUri( getFormUri() );
        ErrorReporter.getInstance().init(getApplicationContext());
        ErrorReporter.getInstance().checkAndSendReports(getApplicationContext());
    }

    public Uri getFormUri() {
        return Uri.parse("http://spreadsheets.google.com/formResponse?formkey=" + getFormId() + "&amp;ifq");
    }

    public abstract String getFormId();

}
