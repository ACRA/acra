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

@ReportsCrashes(formKey = "dFpaTk5GZ1phemI4c0hwMkxYZWh0M1E6MQ",
        mode = ReportingInteractionMode.NOTIFICATION,
        resToastText = R.string.crash_toast_text,
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        additionalDropBoxTags = { "titi", "tata", "toto"},
        includeEventsLogcat = true,
        includeRadioLogcat = true)
public class CrashTest extends Application {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        ACRA.init(this);
        addDropBoxEvents();
        super.onCreate();
    }

    private void addDropBoxEvents() {
//        DropBoxManager dbm = (DropBoxManager) getSystemService(DROPBOX_SERVICE);
//        dbm.addText("toto", "gabü");
//        dbm.addText("tata", "zhô");
//        dbm.addText("titi", "meuh");
    }

}
