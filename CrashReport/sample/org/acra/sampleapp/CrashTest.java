package org.acra.sampleapp;

import org.acra.CrashReportingApplication;

public class CrashTest extends CrashReportingApplication {

    @Override
    public String getFormId() {
        return "dEM4SDNGX0tvaDVxSjk0NVM5ZTl4Y3c6MQ";
    }

    /* (non-Javadoc)
     * @see org.acra.CrashReportingApplication#getToastTextResource()
     */
    @Override
    public int getToastTextResource() {
        return R.string.crash_toast_text;
    }

    /* (non-Javadoc)
     * @see org.acra.CrashReportingApplication#getReportingInteractionMode()
     */
    @Override
    public ReportingInteractionMode getReportingInteractionMode() {
        return ReportingInteractionMode.NOTIFICATION;
    }

    
}
