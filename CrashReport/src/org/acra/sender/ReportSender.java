package org.acra.sender;

import org.acra.CrashReportData;


public interface ReportSender {
    public void send(CrashReportData errorContent) throws ReportSenderException;
}
