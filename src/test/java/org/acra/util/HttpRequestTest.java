package org.acra.util;


import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.log.NonAndroidLog;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Responsible for testing HttpRequest.
 * <p/>
 * User: William
 * Date: 17/07/11
 * Time: 9:37 AM
 */
public class HttpRequestTest {

    // This is a form for a publicly viewable GoogleDoc
    // The Doc is viewable at https://spreadsheets.google.com/spreadsheet/ccc?key=0Al8DtaRlEtcodDAtVDRabnhvZVdIOUxXWTFEM3gzSlE
    private static final String FORM_KEY = "dDAtVDRabnhvZVdIOUxXWTFEM3gzSlE6MQ";

    @Before
    public void setUp() throws Exception {
        final NonAndroidLog log = new NonAndroidLog();
        ACRA.setLog(log);
        ACRA.getConfig().setDisableSSLCertValidation(true);
    }


    @Test
    public void testSocketTimeOutCausesRequestToBeRetriedSeveralTimes_Issue63() throws Exception {

        final URL url = new URL("https://spreadsheets.google.com/formResponse?formkey=" + FORM_KEY + "&amp;ifq");
        final Map<String, String> params = new HashMap<String, String>();

        // Values observed in the GoogleDocs original html form. I presume they are required to ensure the GoogleDoc form is posted to the spreadsheet.
        params.put("pageNumber", "0");
        params.put("backupCache", "");
        params.put("submit", "Envoyer");

        params.put("entry.0.single", "HttpRequestTest#testIssue63");
        params.put("entry.1.single", new Date().toString());

        final HttpRequest request = new HttpRequest();
        request.setSocketTimeOut(100); // Set a very low SocketTimeOut. Something that will almost certainly fail.
        request.setMaxNrRetries(0);

        try {
            request.send(url, Method.POST, HttpRequest.getParamsAsFormString(params), Type.FORM);
            Assert.fail("Should not be able to get a response with an impossibly low SocketTimeOut");
        } catch (SocketTimeoutException e) {
            // as expected.
        }

        // Tell the HttpRequest to retry on Socket time out.
        request.setMaxNrRetries(5);
        try {
            request.send(url, Method.POST, HttpRequest.getParamsAsFormString(params), Type.FORM);
        } catch (SocketTimeoutException e) {
            Assert.fail("Should not get a SocketTimeOut when using SocketTimeOutRetryHandler");
        }
    }
}
