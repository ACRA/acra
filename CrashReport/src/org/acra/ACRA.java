package org.acra;

import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class ACRA {
    protected static final String LOG_TAG = "ACRA";

    /**
     * Bundle key for the icon in the status bar notification.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_NOTIF_ICON = "RES_NOTIF_ICON";
    /**
     * Bundle key for the ticker text in the status bar notification.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_NOTIF_TICKER_TEXT = "RES_NOTIF_TICKER_TEXT";
    /**
     * Bundle key for the title in the status bar notification.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_NOTIF_TITLE = "RES_NOTIF_TITLE";
    /**
     * Bundle key for the text in the status bar notification.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_NOTIF_TEXT = "RES_NOTIF_TEXT";
    /**
     * Bundle key for the icon in the crash dialog.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_ICON = "RES_DIALOG_ICON";
    /**
     * Bundle key for the title in the crash dialog.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_TITLE = "RES_DIALOG_TITLE";
    /**
     * Bundle key for the text in the crash dialog.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_TEXT = "RES_DIALOG_TEXT";
    /**
     * Bundle key for the user comment input label in the crash dialog. If not
     * provided, disables the input field.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_COMMENT_PROMPT = "RES_DIALOG_COMMENT_PROMPT";
    /**
     * Bundle key for the Toast text triggered when the user accepts to send a
     * report in the crash dialog.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_OK_TOAST = "RES_DIALOG_OK_TOAST";
    /**
     * Bundle key for the Toast text triggered when the application crashes if
     * the notification+dialog mode is not used.
     * 
     * @see #getCrashResources()
     */
    public static final String RES_TOAST_TEXT = "RES_TOAST_TEXT";

    /**
     * This is the identifier (value = 666) use for the status bar notification
     * issued when crashes occur.
     */
    public static final int NOTIF_CRASH_ID = 666;

    /**
     * The key of the application default SharedPreference where you can put a
     * 'true' Boolean value to disable ACRA.
     */
    public static final String PREF_DISABLE_ACRA = "acra.disable";

    /**
     * Alternatively, you can use this key if you prefer your users to have the
     * checkbox ticked to enable crash reports. If both acra.disable and
     * acra.enable are set, the value of acra.disable takes over the other.
     */
    public static final String PREF_ENABLE_ACRA = "acra.enable";

    private static Application mApplication;
    private static ReportsCrashes mReportsCrashes;
    private static Bundle mCrashResources;

    public static void init(Application app) {
        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes != null) {

            SharedPreferences prefs = getACRASharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (PREF_DISABLE_ACRA.equals(key) || PREF_ENABLE_ACRA.equals(key)) {
                        Boolean disableAcra = false;
                        try {
                            disableAcra = sharedPreferences.getBoolean(PREF_DISABLE_ACRA, !sharedPreferences.getBoolean(PREF_ENABLE_ACRA, true));
                        } catch (Exception e) {
                            // In case of a ClassCastException
                        }
                        if (disableAcra) {
                            ErrorReporter.getInstance().disable();
                        } else {
                            try {
                                initAcra();
                            } catch (ACRAConfigurationException e) {
                                Log.e(LOG_TAG, "Error : ", e);
                            }
                        }
                    }

                }
            });

            // If the application default shared preferences contains true for
            // the
            // key "acra.disable", do not activate ACRA. Also checks the
            // alternative
            // opposite setting "acra.enable" if "acra.disable" is not found.
            boolean disableAcra = false;
            try {
                disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !prefs.getBoolean(PREF_ENABLE_ACRA, true));
            } catch (Exception e) {
                // In case of a ClassCastException
            }

            if (disableAcra) {
                Log.d(LOG_TAG, "ACRA is disabled for " + mApplication.getPackageName() + ".");
                return;
            } else {
                try {
                    initAcra();
                } catch (ACRAConfigurationException e) {
                    Log.e(LOG_TAG, "Error : ", e);
                }
            }

        }
    }

    /**
     * Activate ACRA.
     * @throws ACRAConfigurationException 
     */
    private static void initAcra() throws ACRAConfigurationException {
        initCrashResources();
        Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");
        // Initialize ErrorReporter with all required data
        ErrorReporter errorReporter = ErrorReporter.getInstance();
        errorReporter.setFormUri(getFormUri());
        errorReporter.setReportingInteractionMode(mReportsCrashes.mode());

        errorReporter.setCrashResources(getCrashResources());

        // Activate the ErrorReporter
        errorReporter.init(mApplication.getApplicationContext());

        // Check for pending reports

        errorReporter.checkReportsOnApplicationStart();
    }


    static void initCrashResources() throws ACRAConfigurationException {
        mCrashResources = new Bundle();
        switch (mReportsCrashes.mode()) {
        case TOAST:
            if(mReportsCrashes.resToastText() == 0) {
                throw new ACRAConfigurationException("TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
            }
            mCrashResources.putInt(RES_TOAST_TEXT, mReportsCrashes.resToastText());
            break;
        case NOTIFICATION:
            mCrashResources.putInt(RES_NOTIF_TICKER_TEXT, mReportsCrashes.resNotifTickerText());
            mCrashResources.putInt(RES_NOTIF_TITLE, mReportsCrashes.resNotifTitle());
            mCrashResources.putInt(RES_NOTIF_TEXT, mReportsCrashes.resNotifText());
            mCrashResources.putInt(RES_NOTIF_ICON, mReportsCrashes.resNotifIcon());
            mCrashResources.putInt(RES_DIALOG_ICON, mReportsCrashes.resDialogIcon());
            mCrashResources.putInt(RES_DIALOG_TITLE, mReportsCrashes.resDialogTitle());
            mCrashResources.putInt(RES_DIALOG_TEXT, mReportsCrashes.resDialogText());
            mCrashResources.putInt(RES_DIALOG_COMMENT_PROMPT, mReportsCrashes.resDialogCommentPrompt());
            mCrashResources.putInt(RES_DIALOG_OK_TOAST, mReportsCrashes.resDialogOkToast());
            break;
        }
    }
    
    static Bundle getCrashResources() {
        return mCrashResources;
    }

    private static Uri getFormUri() {

        return mReportsCrashes.formUri().equals("") ? Uri.parse("http://spreadsheets.google.com/formResponse?formkey="
                + mReportsCrashes.value() + "&amp;ifq") : Uri.parse(mReportsCrashes.formUri());
    }

    /**
     * Override this method if you need to store "acra.disable" or "acra.enable"
     * in a different SharedPrefence than the application's default.
     * 
     * @return The Shared Preferences where ACRA will check the value of the
     *         setting which disables/enables it's action.
     */
    public static SharedPreferences getACRASharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

}
