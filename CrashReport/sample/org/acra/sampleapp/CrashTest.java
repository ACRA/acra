package org.acra.sampleapp;

import org.acra.CrashReportingApplication;

import android.os.Bundle;

public class CrashTest extends CrashReportingApplication {

    @Override
    public String getFormId() {
        return "dEM4SDNGX0tvaDVxSjk0NVM5ZTl4Y3c6MQ";
    }

    @Override
    public Bundle getCrashResources() {
        // Silent Mode
        // return null;
        
        // Toast mode
        return getToastCrashResources();
        
        // Notification mode with mandatory resources
        //return getMinNotificationCrashResources();
        
        // Notification mode with all resources
        //return getFullNotificationCrashResources();
    }
    
    private Bundle getToastCrashResources() {
        Bundle result = new Bundle();
        result.putInt(RES_TOAST_TEXT, R.string.crash_toast_text);
        return result;
    }
    
    private Bundle getFullNotificationCrashResources() {
        Bundle result = new Bundle();
        result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error);
        result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
        result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
        result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
        result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info);
        result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title);
        result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
        result.putInt(RES_DIALOG_COMMENT_PROMPT, R.string.crash_dialog_comment_prompt);
        result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast);
        return result;
    }
    
    private Bundle getMinNotificationCrashResources() {
        Bundle result = new Bundle();
        result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
        result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
        result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
        result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
        return result;
    }
}
