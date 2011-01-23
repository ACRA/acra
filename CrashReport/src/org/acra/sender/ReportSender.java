package org.acra.sender;

import java.util.Map;

import org.acra.ReportField;


public interface ReportSender {
    public void send(Map<ReportField, String> errorContent) throws ReportSenderException;
}
