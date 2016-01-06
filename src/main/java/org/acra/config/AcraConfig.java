package org.acra.config;

import org.acra.annotation.ReportsCrashes;

import java.security.KeyStore;
import java.util.Map;

/**
 * Configuration for ACRA.
 *
 * Declarative forms of this are made using the <pre>@ReportsCrashes</pre> annotation
 * on the Application class.
 *
 * Programmatic versions can be configured using
 */
public interface AcraConfig extends ReportsCrashes {

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
}
