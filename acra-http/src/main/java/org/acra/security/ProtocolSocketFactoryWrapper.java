/*
 * Copyright (c) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.security;

import android.os.Build;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolSocketFactoryWrapper extends SSLSocketFactory {
    public static final String TLS_v1 = "TLSv1";
    public static final String TLS_v1_1 = "TLSv1.1";
    public static final String TLS_v1_2 = "TLSv1.2";
    public static final String TLS_v1_3 = "TLSv1.3";
    private final SSLSocketFactory delegate;
    private final List<String> protocols;

    public ProtocolSocketFactoryWrapper(SSLSocketFactory delegate, List<String> protocols) {
        this.delegate = delegate;
        this.protocols = new ArrayList<>(protocols);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            this.protocols.remove(TLS_v1_3);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                this.protocols.remove(TLS_v1_2);
                this.protocols.remove(TLS_v1_1);
            }
        }
    }

    private Socket setProtocols(Socket socket) {
        if ((socket instanceof SSLSocket) && isTLSServerEnabled((SSLSocket) socket)) {
            ((SSLSocket) socket).setEnabledProtocols(protocols.toArray(new String[0]));
        }
        return socket;
    }

    private boolean isTLSServerEnabled(SSLSocket sslSocket) {
        for (String protocol : sslSocket.getSupportedProtocols()) {
            if (protocols.contains(protocol)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
        return setProtocols(delegate.createSocket(socket, s, i, b));
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        return setProtocols(delegate.createSocket(s, i));
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
        return setProtocols(delegate.createSocket(s, i, inetAddress, i1));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return setProtocols(delegate.createSocket(inetAddress, i));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return setProtocols(delegate.createSocket(inetAddress, i, inetAddress1, i1));
    }
}
