package org.acra.sender;

import android.content.Context;

/**
 * Factory for creating and configuring a {@link ReportSender} instance.
 * Implementations must have a no argument constructor.
 *
 * Each configured ReportSenderFactory is created within the {@link org.acra.SenderService}
 * and is used to contruct and configure a single {@link ReportSender}.
 *
 * Created by William on 4-JAN-2016.
 */
public interface ReportSenderFactory {

    /**
     * @param context   Application context.
     * @return Fully configured instance of the relevant ReportSender.
     */
    ReportSender create(Context context);
}
