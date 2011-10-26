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

import static org.acra.ReportField.*;

import java.lang.annotation.Annotation;

import org.acra.annotation.ReportsCrashes;
import org.acra.sender.EmailIntentSender;
import org.acra.sender.GoogleFormSender;
import org.acra.sender.HttpPostSender;
import org.acra.util.PackageManagerWrapper;

import android.Manifest.permission;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Use this class to initialize the crash reporting feature using
 * {@link #init(Application)} as soon as possible in your {@link Application}
 * subclass {@link Application#onCreate()} method. Configuration items must have
 * been set by using {@link ReportsCrashes} above the declaration of your
 * {@link Application} subclass.
 * 
 * @author Kevin Gaudin
 * 
 */
public class ACRA {

    public static final boolean DEV_LOGGING = false; // Should be false for release.
    public static final String LOG_TAG = ACRA.class.getSimpleName();

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

    /**
     * The key of the SharedPreference allowing the user to disable sending
     * content of logcat/dropbox. System logs collection is also dependent of
     * the READ_LOGS permission.
     */
    public static final String PREF_ENABLE_SYSTEM_LOGS = "acra.syslog.enable";

    /**
     * The key of the SharedPreference allowing the user to disable sending his
     * device id. Device ID collection is also dependent of the READ_PHONE_STATE
     * permission.
     */
    public static final String PREF_ENABLE_DEVICE_ID = "acra.deviceid.enable";

    /**
     * The key of the SharedPreference allowing the user to always include his
     * email address.
     */
    public static final String PREF_USER_EMAIL_ADDRESS = "acra.user.email";

    /**
     * The key of the SharedPreference allowing the user to automatically accept
     * sending reports.
     */
    public static final String PREF_ALWAYS_ACCEPT = "acra.alwaysaccept";

    private static Application mApplication;
    private static ReportsCrashes mReportsCrashes;

    // Accessible via ACRA#getErrorReporter().
    private static ErrorReporter errorReporterSingleton;

    // NB don't convert to a local field because then it could be garbage collected and then we would have no PreferenceListener.
    private static OnSharedPreferenceChangeListener mPrefListener;

    /**
     * <p>
     * Initialize ACRA for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#onCreate()}
     * method.
     * </p>
     * 
     * @param app   Your Application class.
     * @throws IllegalStateException if it is called more than once.
     */
    public static void init(Application app) {

        if (mApplication != null) {
            throw new IllegalStateException("ACRA#init called more than once");
        }

        mApplication = app;
        mReportsCrashes = mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes == null) {
            Log.e(LOG_TAG, "ACRA#init called but no ReportsCrashes annotation on Application " + mApplication.getPackageName());
            return;
        }
        
        final SharedPreferences prefs = getACRASharedPreferences();
        Log.d(ACRA.LOG_TAG, "Set OnSharedPreferenceChangeListener.");

        try {
            checkCrashResources();

            Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");

            // Initialize ErrorReporter with all required data
            final boolean enableAcra = !shouldDisableACRA(prefs);
            final ErrorReporter errorReporter = new ErrorReporter(mApplication.getApplicationContext(), prefs, enableAcra);

            // Append ReportSenders.
            addReportSenders(errorReporter);

            errorReporterSingleton = errorReporter;

        } catch (ACRAConfigurationException e) {
            Log.w(LOG_TAG, "Error : ", e);
        }

        // We HAVE to keep a reference otherwise the listener could be garbage collected:
        // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
        mPrefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (PREF_DISABLE_ACRA.equals(key) || PREF_ENABLE_ACRA.equals(key)) {
                    final boolean enableAcra = !shouldDisableACRA(sharedPreferences);
                    getErrorReporter().setEnabled(enableAcra);
                }
            }
        };

        // This listener has to be set after initAcra is called to avoid a
        // NPE in ErrorReporter.disable() because
        // the context could be null at this moment.
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    /**
     * @return the current instance of ErrorReporter.
     * @throws IllegalStateException if {@link ACRA#init(android.app.Application)} has not yet been called.
     */
    public static ErrorReporter getErrorReporter() {
        if (errorReporterSingleton == null) {
            throw  new IllegalStateException("Cannot access ErrorReporter before ACRA#init");
        }
        return errorReporterSingleton;
    }



    /**
     * Adds any relevant ReportSenders to the ErrorReporter.
     *
     * @param errorReporter ErrorReporter to which to add appropriate ReportSenders.
     */
    private static void addReportSenders(ErrorReporter errorReporter) {

        // Try to send by mail.
        if (!"".equals(mReportsCrashes.mailTo())) {
            Log.w(LOG_TAG, mApplication.getPackageName() + " reports will be sent by email (if accepted by user).");
            errorReporter.addReportSender(new EmailIntentSender(mApplication));
            return;
        }

        final PackageManagerWrapper pm = new PackageManagerWrapper(mApplication);
        if (!pm.hasPermission(permission.INTERNET)) {
            // NB If the PackageManager has died then this will erroneously log the error that the App doesn't have Internet (even though it does).
            // I think that is a small price to pay to ensure that ACRA doesn't crash if the PackageManager has died.
            Log.e(LOG_TAG, mApplication.getPackageName()
                            + " should be granted permission "
                            + permission.INTERNET
                            + " if you want your crash reports to be sent. If you don't want to add this permission to your application you can also enable sending reports by email. If this is your will then provide your email address in @ReportsCrashes(mailTo=\"your.account@domain.com\"");
            return;
        }

        // If formUri is set, instantiate a sender for a generic HTTP POST form
        if (mReportsCrashes.formUri() != null && !"".equals(mReportsCrashes.formUri())) {
            errorReporter.addReportSender(new HttpPostSender(mReportsCrashes.formUri(), null));
            return;
        }

        // The default behavior is to use the formKey for a Google Docs Form.
        if (mReportsCrashes.formKey() != null && !"".equals(mReportsCrashes.formKey().trim())) {
            errorReporter.addReportSender(new GoogleFormSender(mReportsCrashes.formKey()));
        }
    }

    /**
     * Check if the application default shared preferences contains true for
     * the key "acra.disable", do not activate ACRA. Also checks the
     * alternative opposite setting "acra.enable" if "acra.disable" is not found.
     *
     * @param prefs SharedPreferences to check to see whether ACRA should be disabled.
     * @return true if prefs indicate that ACRA should be disabled.
     */
    private static boolean shouldDisableACRA(SharedPreferences prefs) {
        boolean disableAcra = false;
        try {
            final boolean enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA, true);
            disableAcra = prefs.getBoolean(PREF_DISABLE_ACRA, !enableAcra);
        } catch (Exception e) {
            // In case of a ClassCastException
        }
        return disableAcra;
    }

    private static void checkCrashResources() throws ACRAConfigurationException {
        switch (mReportsCrashes.mode()) {
        case TOAST:
            if (mReportsCrashes.resToastText() == 0) {
                throw new ACRAConfigurationException(
                        "TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
            }
            break;
        case NOTIFICATION:
            if (mReportsCrashes.resNotifTickerText() == 0 || mReportsCrashes.resNotifTitle() == 0
                    || mReportsCrashes.resNotifText() == 0 || mReportsCrashes.resDialogText() == 0) {
                throw new ACRAConfigurationException(
                        "NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText, resDialogText parameters in your application @ReportsCrashes() annotation.");
            }
            break;
        }
    }

    /**
     * Retrieves the {@link SharedPreferences} instance where user adjustable
     * settings for ACRA are stored. Default are the Application default
     * SharedPreferences, but you can provide another SharedPreferences name
     * with {@link ReportsCrashes#sharedPreferencesName()}.
     * 
     * @return The Shared Preferences where ACRA will retrieve its user adjustable setting.
     */
    public static SharedPreferences getACRASharedPreferences() {
        // TODO is there any reason to keep this method public? If we can hide it, we should. Do clients ever need to access it?
        if (!"".equals(mReportsCrashes.sharedPreferencesName())) {
            Log.d(ACRA.LOG_TAG, "Retrieve SharedPreferences " + mReportsCrashes.sharedPreferencesName());
            return mApplication.getSharedPreferences(mReportsCrashes.sharedPreferencesName(),
                    mReportsCrashes.sharedPreferencesMode());
        } else {
            Log.d(ACRA.LOG_TAG, "Retrieve application default SharedPreferences.");
            return PreferenceManager.getDefaultSharedPreferences(mApplication);
        }
    }

    /**
     * Provides the configuration annotation instance.
     * @return ACRA {@link ReportsCrashes} configuration instance.
     */
    public static ReportsCrashes getConfig() {
        if(configProxy == null) {
            configProxy = new ReportsCrashes() {
                
                @Override
                public Class<? extends Annotation> annotationType() {
                    return mReportsCrashes.annotationType();
                }
                
                @Override
                public int socketTimeout() {
                    return mReportsCrashes.socketTimeout();
                }
                
                @Override
                public String sharedPreferencesName() {
                    return mReportsCrashes.sharedPreferencesName();
                }
                
                @Override
                public int sharedPreferencesMode() {
                    return mReportsCrashes.sharedPreferencesMode();
                }
                
                @Override
                public int resToastText() {
                    if(RES_TOAST_TEXT != null) { 
                        return RES_TOAST_TEXT;
                    } else {
                        return mReportsCrashes.resToastText();
                    }
                }
                
                @Override
                public int resNotifTitle() {
                    if(RES_NOTIF_TITLE != null) { 
                        return RES_NOTIF_TITLE;
                    } else {
                        return mReportsCrashes.resNotifTitle();
                    }
                }
                
                @Override
                public int resNotifTickerText() {
                    if(RES_NOTIF_TICKER_TEXT != null) { 
                        return RES_NOTIF_TICKER_TEXT;
                    } else {
                        return mReportsCrashes.resNotifTickerText();
                    }
                }
                
                @Override
                public int resNotifText() {
                    if(RES_NOTIF_TEXT != null) { 
                        return RES_NOTIF_TEXT;
                    } else {
                        return mReportsCrashes.resNotifText();
                    }
                }
                
                @Override
                public int resNotifIcon() {
                    if(RES_NOTIF_ICON != null) { 
                        return RES_NOTIF_ICON;
                    } else {
                        return mReportsCrashes.resNotifIcon();
                    }
                }
                
                @Override
                public int resDialogTitle() {
                    if(RES_DIALOG_TITLE != null) { 
                        return RES_DIALOG_TITLE;
                    } else {
                        return mReportsCrashes.resDialogTitle();
                    }
                }
                
                @Override
                public int resDialogText() {
                    if(RES_DIALOG_TEXT != null) { 
                        return RES_DIALOG_TEXT;
                    } else {
                        return mReportsCrashes.resDialogText();
                    }
                }
                
                @Override
                public int resDialogOkToast() {
                    if(RES_DIALOG_OK_TOAST != null) { 
                        return RES_DIALOG_OK_TOAST;
                    } else {
                        return mReportsCrashes.resDialogOkToast();
                    }
                }
                
                @Override
                public int resDialogIcon() {
                    if(RES_DIALOG_ICON != null) { 
                        return RES_DIALOG_ICON;
                    } else {
                        return mReportsCrashes.resDialogIcon();
                    }
                }
                
                @Override
                public int resDialogEmailPrompt() {
                    if(RES_DIALOG_EMAIL_PROMPT != null) { 
                        return RES_DIALOG_EMAIL_PROMPT;
                    } else {
                        return mReportsCrashes.resDialogEmailPrompt();
                    }
                }
                
                @Override
                public int resDialogCommentPrompt() {
                    if(RES_DIALOG_COMMENT_PROMPT != null) { 
                        return RES_DIALOG_COMMENT_PROMPT;
                    } else {
                        return mReportsCrashes.resDialogCommentPrompt();
                    }
                }
                
                @Override
                public ReportingInteractionMode mode() {
                    return mReportsCrashes.mode();
                }
                
                @Override
                public int maxNumberOfRequestRetries() {
                    return mReportsCrashes.maxNumberOfRequestRetries();
                }
                
                @Override
                public String mailTo() {
                    return mReportsCrashes.mailTo();
                }
                
                @Override
                public String[] logcatArguments() {
                    return mReportsCrashes.logcatArguments();
                }
                
                @Override
                public boolean includeDropBoxSystemTags() {
                    return mReportsCrashes.includeDropBoxSystemTags();
                }
                
                @Override
                public String formUriBasicAuthPassword() {
                    return mReportsCrashes.formUriBasicAuthPassword();
                }
                
                @Override
                public String formUriBasicAuthLogin() {
                    return mReportsCrashes.formUriBasicAuthLogin();
                }
                
                @Override
                public String formUri() {
                    return mReportsCrashes.formUri();
                }
                
                @Override
                public String formKey() {
                    return mReportsCrashes.formKey();
                }
                
                @Override
                public boolean forceCloseDialogAfterToast() {
                    return mReportsCrashes.forceCloseDialogAfterToast();
                }
                
                @Override
                public int dropboxCollectionMinutes() {
                    return mReportsCrashes.dropboxCollectionMinutes();
                }
                
                @Override
                public boolean deleteUnapprovedReportsOnApplicationStart() {
                    return mReportsCrashes.deleteUnapprovedReportsOnApplicationStart();
                }
                
                @Override
                public ReportField[] customReportContent() {
                    return mReportsCrashes.customReportContent();
                }
                
                @Override
                public int connectionTimeout() {
                    return mReportsCrashes.connectionTimeout();
                }
                
                @Override
                public String[] additionalSharedPreferences() {
                    return mReportsCrashes.additionalSharedPreferences();
                }
                
                @Override
                public String[] additionalDropBoxTags() {
                    return mReportsCrashes.additionalDropBoxTags();
                }
            };
        }
        return configProxy;
    }

    /**
     * Default list of {@link ReportField}s to be sent in email reports.
     * You can set your own list with {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     *
     * @see org.acra.annotation.ReportsCrashes#mailTo()
     */
    public final static ReportField[] DEFAULT_MAIL_REPORT_FIELDS = { ReportField.USER_COMMENT, ReportField.ANDROID_VERSION,
            ReportField.APP_VERSION_NAME, ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
            ReportField.STACK_TRACE };

    /**
     * Default list of {@link ReportField}s to be sent in reports. You can set your own list with
     * {@link org.acra.annotation.ReportsCrashes#customReportContent()}.
     */
    public static final ReportField[] DEFAULT_REPORT_FIELDS = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME,
    FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, BUILD, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE,
    CUSTOM_DATA, IS_SILENT, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION, DISPLAY, USER_COMMENT,
    USER_EMAIL, USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, DROPBOX, LOGCAT, EVENTSLOG, RADIOLOG,
 DEVICE_ID, INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SHARED_PREFERENCES,
            SETTINGS_SYSTEM, SETTINGS_SECURE };

    /**
     * A special String value to allow the usage of a pseudo-null default value in annotation parameters.
     */
    public static final String NULL_VALUE = "ACRA-NULL-STRING";

    
    /**
     * Since ADT v14, when using Android Library Projects, resource Ids can't be passed as annotation parameter values anymore.
     * In this case, devs can use setters to pass their Ids. These setters have to be called before {@link ACRA#init(Application)}.
     * This method is called early in {@link ACRA#init(Application)} to initialize the {@link ReportsCrashes} annotation with values
     * passed in the setters.
     */
    private static Integer RES_DIALOG_COMMENT_PROMPT = null;
    private static Integer RES_DIALOG_EMAIL_PROMPT = null;
    private static Integer RES_DIALOG_ICON = null;
    private static Integer RES_DIALOG_OK_TOAST = null;
    private static Integer RES_DIALOG_TEXT = null;
    private static Integer RES_DIALOG_TITLE = null;
    private static Integer RES_NOTIF_ICON = null;
    private static Integer RES_NOTIF_TEXT = null;
    private static Integer RES_NOTIF_TICKER_TEXT = null;
    private static Integer RES_NOTIF_TITLE = null;
    private static Integer RES_TOAST_TEXT = null;
    private static ReportsCrashes configProxy;
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogCommentPrompt()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogCommentPrompt()}
     */
    public static void setResDialogCommentPrompt(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogCommentPrompt(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_COMMENT_PROMPT = resId;
    }

    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogEmailPrompt()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogEmailPrompt()}
     */
    public static void setResDialogEmailPrompt(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogEmailPrompt(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_EMAIL_PROMPT = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogIcon()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogIcon()}
     */
    public static void setResDialogIcon(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogIcon(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_ICON = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogOkToast()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     */
    public static void setResDialogOkToast(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogOkToast(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_OK_TOAST = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogText()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogText()}
     */
    public static void setResDialogText(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogText(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_TEXT = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resDialogTitle()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resDialogTitle()}
     */
    public static void setResDialogTitle(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResDialogTitle(int) before ACRA.init(Application).");
            return;
        }
        RES_DIALOG_TITLE = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resNotifIcon()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resNotifIcon()}
     */
    public static void setResNotifIcon(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResNotifIcon(int) before ACRA.init(Application).");
            return;
        }
        RES_NOTIF_ICON = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resNotifText()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resNotifText()}
     */
    public static void setResNotifText(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResNotifText(int) before ACRA.init(Application).");
            return;
        }
        RES_NOTIF_TEXT = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resNotifTickerText()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resNotifTickerText()}
     */
    public static void setResNotifTickerText(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResNotifTickerText(int) before ACRA.init(Application).");
            return;
        }
        RES_NOTIF_TICKER_TEXT = resId;
    }
    
    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resNotifTitle()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resNotifTitle()}
     */
    public static void setResNotifTitle(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResNotifTitle(int) before ACRA.init(Application).");
            return;
        }
        RES_NOTIF_TITLE = resId;
    }

    /**
     * Use this method BEFORE calling {@link ACRA#init(Application)} if the id you wanted to give to {@link ReportsCrashes#resToastText()}
     * comes from an Android Library Project.
     * @param resId The resource id, see {@link ReportsCrashes#resToastText()}
     */
    public static void setResToastText(int resId) {
        if(mApplication != null) {
            Log.e(LOG_TAG, "ACRA has already been initialized. You should call setResToastText(int) before ACRA.init(Application).");
            return;
        }
        RES_TOAST_TEXT = resId;
    }

}
