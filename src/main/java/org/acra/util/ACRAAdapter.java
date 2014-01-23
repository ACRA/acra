package org.acra.util;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class ACRAAdapter {

	private static SocketFactory mSocketFactory;

	public static SocketFactory getSocketFactory() {	
		return mSocketFactory==null ? SSLSocketFactory.getSocketFactory() : mSocketFactory;
	}

	public static void setSSLSocketFactory(SocketFactory factory){
		mSocketFactory=factory;
	}
}
