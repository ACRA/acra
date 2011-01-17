package org.acra.sender;

@SuppressWarnings("serial")
public class ReportSenderException extends Exception {

    public ReportSenderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
