package org.acra;

import android.app.IntentService;
import android.content.Intent;
import org.acra.sender.ReportSender;

import java.io.Serializable;
import java.util.ArrayList;

public class SenderService extends IntentService {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_APPROVE_REPORTS_FIRST = "approveReportsFirst";
    public static final String EXTRA_REPORT_SENDERS = "reportSenders";

    public SenderService() {
        super("ACRA Report Sender");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        final boolean approveReportsFirst = intent.getBooleanExtra(EXTRA_APPROVE_REPORTS_FIRST, false);
        final Serializable reportSenders = intent.getSerializableExtra(EXTRA_REPORT_SENDERS);

        try {
            //noinspection unchecked
            final ArrayList<Class<? extends ReportSender>> senderClasses = (ArrayList<Class<? extends ReportSender>>) reportSenders;
            final ArrayList<ReportSender> senderInstances = new ArrayList<ReportSender>();
            for (final Class<? extends ReportSender> senderClass : senderClasses) {
                senderInstances.add(senderClass.newInstance());
            }

            new SendWorker(this, senderInstances, onlySendSilentReports, approveReportsFirst).run();
        } catch (Exception e) {
            ACRA.log.e(ACRA.class.getSimpleName(), "", e);
        }
    }
}
