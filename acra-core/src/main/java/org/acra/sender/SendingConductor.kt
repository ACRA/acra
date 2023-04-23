package org.acra.sender

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import org.acra.ACRAConstants
import org.acra.config.CoreConfiguration
import org.acra.file.CrashReportFileNameParser
import org.acra.file.ReportLocator
import org.acra.log.debug
import org.acra.log.error
import org.acra.plugins.loadEnabled
import org.acra.util.ToastSender.sendToast

/**
 * @author Lukas
 * @since 31.12.2018
 */
class SendingConductor(private val context: Context, private val config: CoreConfiguration) {
    private val locator: ReportLocator = ReportLocator(context)

    fun sendReports(foreground: Boolean, extras: Bundle) {
        debug { "About to start sending reports from SenderService" }
        try {
            val senderInstances = ReportSender.loadSenders(context, config).filter { it.requiresForeground() == foreground }.toMutableList()
            if (senderInstances.isEmpty()) {
                debug { "No ReportSenders configured - adding NullSender" }
                senderInstances.add(NullSender())
            }

            // Get approved reports
            val reports = locator.approvedReports
            val reportDistributor = ReportDistributor(context, config, senderInstances, extras)

            // Iterate over approved reports and send via all Senders.
            var reportsSentCount = 0 // Use to rate limit sending
            val fileNameParser = CrashReportFileNameParser()
            var anyNonSilent = false
            for (report in reports) {
                val isNonSilent = !fileNameParser.isSilent(report.name)
                if (extras.getBoolean(LegacySenderService.EXTRA_ONLY_SEND_SILENT_REPORTS) && isNonSilent) {
                    continue
                }
                anyNonSilent = anyNonSilent or isNonSilent
                if (reportsSentCount >= ACRAConstants.MAX_SEND_REPORTS) {
                    break // send only a few reports to avoid overloading the network
                }
                if (reportDistributor.distribute(report)) {
                    reportsSentCount++
                }
            }
            val toast: String? = if (reportsSentCount > 0) config.reportSendSuccessToast else config.reportSendFailureToast
            if (anyNonSilent && !toast.isNullOrEmpty()) {
                debug { "About to show " + (if (reportsSentCount > 0) "success" else "failure") + " toast" }
                Handler(Looper.getMainLooper()).post { sendToast(context, toast, Toast.LENGTH_LONG) }
            }
        } catch (e: Exception) {
            error(e) { "" }
        }
        debug { "Finished sending reports from SenderService" }
    }
}