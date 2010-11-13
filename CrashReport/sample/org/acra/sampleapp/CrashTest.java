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

package org.acra.sampleapp;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formId = "dEM4SDNGX0tvaDVxSjk0NVM5ZTl4Y3c6MQ",
        mode = ReportingInteractionMode.NOTIFICATION,
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt)
public class CrashTest extends Application {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }

    // private Bundle getToastCrashResources() {
    // Bundle result = new Bundle();
    // result.putInt(RES_TOAST_TEXT, R.string.crash_toast_text);
    // return result;
    // }
    //
    // private Bundle getFullNotificationCrashResources() {
    // Bundle result = new Bundle();
    // result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error);
    // result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
    // result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
    // result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
    // result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info);
    // result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title);
    // result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
    // result.putInt(RES_DIALOG_COMMENT_PROMPT,
    // R.string.crash_dialog_comment_prompt);
    // result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast);
    // return result;
    // }
    //
    // private Bundle getMinNotificationCrashResources() {
    // Bundle result = new Bundle();
    // result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
    // result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
    // result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
    // result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
    // return result;
    // }
}
