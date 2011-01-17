package org.acra.sender;

import java.util.Properties;

public interface ReportSender {
    public void send(Properties report) throws ReportSenderException;
}
