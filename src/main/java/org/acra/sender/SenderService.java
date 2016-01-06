package org.acra.sender;

import android.app.IntentService;
import android.content.Intent;
import org.acra.ACRA;
import org.acra.config.AcraConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class SenderService extends IntentService {

    public static final String EXTRA_ONLY_SEND_SILENT_REPORTS = "onlySendSilentReports";
    public static final String EXTRA_APPROVE_REPORTS_FIRST = "approveReportsFirst";
    public static final String EXTRA_REPORT_SENDER_FACTORIES = "reportSenderFactories";
    public static final String EXTRA_ACRA_CONFIG = "acraConfig";

    public SenderService() {
        super("ACRA SenderService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        final boolean onlySendSilentReports = intent.getBooleanExtra(EXTRA_ONLY_SEND_SILENT_REPORTS, false);
        final boolean approveReportsFirst = intent.getBooleanExtra(EXTRA_APPROVE_REPORTS_FIRST, false);
        final Serializable reportSenderFactoryClasses = intent.getSerializableExtra(EXTRA_REPORT_SENDER_FACTORIES);
        final AcraConfig config = (AcraConfig) intent.getSerializableExtra(EXTRA_ACRA_CONFIG);

        ACRA.log.v(LOG_TAG, "About to start sending reports from SenderService");
        try {
            //noinspection unchecked
            final ArrayList<Class<? extends ReportSenderFactory>> senderFactoryClasses = (ArrayList<Class<? extends ReportSenderFactory>>) reportSenderFactoryClasses;
            final List<ReportSender> senderInstances = getSenderInstances(config, senderFactoryClasses);
            new SendWorker(this, config, senderInstances, onlySendSilentReports, approveReportsFirst).run();
        } catch (Exception e) {
            ACRA.log.e(ACRA.class.getSimpleName(), "", e);
        }
    }

    private List<ReportSender> getSenderInstances(AcraConfig config, List<Class<? extends ReportSenderFactory>> factoryClasses) {
        final List<ReportSender> reportSenders = new ArrayList<ReportSender>();
        for (final Class<? extends ReportSenderFactory> factoryClass : factoryClasses) {
            try {
                final ReportSenderFactory factory = factoryClass.newInstance();
                final ReportSender sender = factory.create(this.getApplication(), config);
                reportSenders.add(sender);
            } catch (InstantiationException e) {
                ACRA.log.w(LOG_TAG, "Could not construct ReportSender from " + factoryClass, e);
            } catch (IllegalAccessException e) {
                ACRA.log.w(LOG_TAG, "Could not construct ReportSender from " + factoryClass, e);
            }
        }
        return reportSenders;
    }
}
