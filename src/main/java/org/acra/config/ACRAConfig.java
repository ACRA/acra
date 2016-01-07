package org.acra.config;

import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.io.Serializable;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/**
 * Configuration for ACRA.
 *
 * Declarative forms of this are made using the <pre>@ReportsCrashes</pre> annotation
 * on the Application class.
 *
 * Programmatic versions can be configured using
 */
public interface ACRAConfig extends ReportsCrashes, Serializable {

    /**
     * Retrieve HTTP headers defined by the application developer. These should
     * be added to requests sent by any third-party sender (over HTTP of
     * course).
     *
     * @return A map associating http header names to their values.
     */
    Map<String, String> getHttpHeaders();

    /**
     * @return KeyStore to use (if any) when sending a HttpsRequest.
     */
    public KeyStore keyStore();

    /**
     * @return List of ReportField that ACRA will provide to the server.
     */
    public List<ReportField> getReportFields();
}
