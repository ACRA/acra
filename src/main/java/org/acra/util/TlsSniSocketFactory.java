/*
 * This class was copied from this blog post:
 * http://blog.dev001.net/post/67082904181/android-using-sni-and-tlsv1-2-with-apache-httpclient
 * Thanks go to Dev001!
 * Also, changes for using only secure cipher suites were included from code of DAVdroid.
 * Thankgs go to Ricki Hirner (bitfire web engineering)!
 */
package org.acra.util;

import android.annotation.TargetApi;
import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * Provides a SSLSocketFactory that is able to use SNI for SSL connections and
 * therefore allows multiple SSL servers on one IP address.<br/>
 *   1) socket = createSocket() is called
 *   2) reasonable encryption settings are applied to socket
 *   3) SNI is set up for socket
 *   4) handshake and certificate/host name verification
 * <p/>
 * @author Philipp Kapfer
 * @since 4.6.0
 */
public class TlsSniSocketFactory implements LayeredSocketFactory {

    private static final String TAG =  TlsSniSocketFactory.class.getSimpleName();
    
    private final static int VERSION_CODES_JELLY_BEAN_MR1 = 17;
    private final static int VERSION_CODES_LOLLIPOP = 21;
    
    private final SSLCertificateSocketFactory sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);

    // use BrowserCompatHostnameVerifier to allow IP addresses in the Common Name
    private final static HostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();

    private static final List<String> ALLOWED_CIPHERS = Arrays.asList(
        // allowed secure ciphers according to NIST.SP.800-52r1.pdf Section 3.3.1 (see http://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-52r1.pdf)
        // TLS 1.2
        "TLS_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECHDE_RSA_WITH_AES_128_GCM_SHA256",
        // maximum interoperability
        "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        // additionally
        "TLS_RSA_WITH_AES_256_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"
    );

    // Plain TCP/IP (layer below TLS)

    @Override
    public Socket connectSocket(Socket s, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        return null;
    }

    @Override
    public Socket createSocket() throws IOException {
        return null;
    }

    @Override
    public boolean isSecure(Socket s) throws IllegalArgumentException {
        return (s instanceof SSLSocket) && s.isConnected();
    }


    // TLS layer

    @Override
    public Socket createSocket(Socket plainSocket, String host, int port, boolean autoClose) throws IOException {
        if (autoClose) {
            // we don't need the plainSocket
            plainSocket.close();
        }

        // create and connect SSL socket, but don't do hostname/certificate verification yet
        final SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(InetAddress.getByName(host), port);

        // establish and verify TLS connection
        establishAndVerify(ssl, host);

        return ssl;
    }
    
    /**
     * Establishes and verifies a TLS connection to a (TCP-)connected SSLSocket:
     *   - set TLS parameters like allowed protocols and ciphers
     *   - set SNI host name
     *   - verify host name
     *   - verify certificate
     * @param socket    unconnected SSLSocket
     * @param host      host name for SNI
     * @throws IOException if the connection could not be established.
     */
    private void establishAndVerify(SSLSocket socket, String host) throws IOException {
        setTlsParameters(socket);
        setSniHostname(socket, host);
        
        // TLS handshake, throws an exception for untrusted certificates
        socket.startHandshake();

        // verify hostname and certificate
        SSLSession session = socket.getSession();
        if (!hostnameVerifier.verify(host, session)) {
            // throw exception for invalid host names
            throw new SSLPeerUnverifiedException(host);
        }

        Log.i(TAG, "Established " + session.getProtocol() + " connection with " + session.getPeerHost() + " using " + session.getCipherSuite());
    }
    
    /**
     * Prepares a TLS/SSL connection socket by:
     *   - setting reasonable TLS protocol versions
     *   - setting reasonable cipher suites (if required)
     * @param socket   unconnected SSLSocket to prepare
     */
    private void setTlsParameters(SSLSocket socket) {
        // Android 5.0+ (API level21) provides reasonable default settings
        // but it still allows SSLv3
        // https://developer.android.com/about/versions/android-5.0-changes.html#ssl

        /* set reasonable protocol versions */
        // - enable all supported protocols (enables TLSv1.1 and TLSv1.2 on Android <5.0)
        // - remove all SSL versions (especially SSLv3) because they're insecure now
        final List<String> protocols = new LinkedList<String>();
        for (String protocol : socket.getSupportedProtocols()) {
            if (!protocol.toUpperCase().contains("SSL")) {
                protocols.add(protocol);
            }
        }
        Log.v(TAG, "Setting allowed TLS protocols: " + TextUtils.join(", ", protocols));
        socket.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));

        /* set reasonable cipher suites */
        if (Build.VERSION.SDK_INT < VERSION_CODES_LOLLIPOP) {
            // choose secure cipher suites

            final List<String> availableCiphers = Arrays.asList(socket.getSupportedCipherSuites());
            
            // preferred ciphers = allowed Ciphers \ availableCiphers
            final Set<String> preferredCiphers = new HashSet<String>(ALLOWED_CIPHERS);
            preferredCiphers.retainAll(availableCiphers);
            
            // add enabled ciphers to preferred ciphers
            // for maximum security, preferred ciphers should *replace* enabled ciphers,
            // but for the security level of ACRA, disabling of insecure
            // ciphers should be a server-side task
            preferredCiphers.addAll(Arrays.asList(socket.getEnabledCipherSuites()));
            
            Log.v(TAG, "Setting allowed TLS ciphers: " + TextUtils.join(", ", preferredCiphers));
            socket.setEnabledCipherSuites(preferredCiphers.toArray(new String[preferredCiphers.size()]));
        }
    }
    
    @TargetApi(VERSION_CODES_JELLY_BEAN_MR1)
    private void setSniHostname(SSLSocket socket, String hostName) {
        // set SNI host name
        if (Build.VERSION.SDK_INT >= VERSION_CODES_JELLY_BEAN_MR1) {
            Log.d(TAG, "Using documented SNI with host name " + hostName);
            sslSocketFactory.setHostname(socket, hostName);
        } else {
            Log.d(TAG, "No documented SNI support on Android <4.2, trying reflection method with host name " + hostName);
            try {
                final Method setHostnameMethod = socket.getClass().getMethod("setHostname", String.class);
                setHostnameMethod.invoke(socket, hostName);
            } catch (Exception e) {
                Log.w(TAG, "SNI not usable", e);
            }
        }
    }
}
