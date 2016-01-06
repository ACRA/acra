package org.acra.sender;

import android.content.Context;
import org.acra.config.AcraConfig;

/**
 * Factory for creating and configuring a {@link ReportSender} instance.
 * Implementations must have a no argument constructor.
 *
 * Each configured ReportSenderFactory is created within the {@link SenderService}
 * and is used to construct and configure a single {@link ReportSender}.
 *
 * Created by William on 4-JAN-2016.
 */
public interface ReportSenderFactory {

    /**
     * @param context   Application context.
     * @param config    Configuration to use when sending reports.
     * @return Fully configured instance of the relevant ReportSender.
     */
    ReportSender create(Context context, AcraConfig config);
}
