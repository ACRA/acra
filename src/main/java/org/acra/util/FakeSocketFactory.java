/*
 * Utility class copied from http://transdroid.googlecode.com with the authorization from Eric Kok to redistribute it under Apache Software License. 
 */
package org.acra.util;

import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FakeSocketFactory implements SocketFactory, LayeredSocketFactory {

    private SSLContext sslcontext = null;

    private static SSLContext createEasySSLContext() throws IOException {
        try {
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new NaiveTrustManager() }, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private SSLContext getSSLContext() throws IOException {
        if (this.sslcontext == null) {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    @Override
    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        final int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        final int soTimeout = HttpConnectionParams.getSoTimeout(params);

        final InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        final SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

        if ((localAddress != null) || (localPort > 0)) {
            // we need to bind explicitly
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }
            final InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
            sslsock.bind(isa);
        }

        sslsock.connect(remoteAddress, connTimeout);
        sslsock.setSoTimeout(soTimeout);

        return sslsock;
    }

    @Override
    public Socket createSocket() throws IOException {
        return getSSLContext().getSocketFactory().createSocket();
    }

    @Override
    public boolean isSecure(Socket arg0) throws IllegalArgumentException {
        return true;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
    }

}
