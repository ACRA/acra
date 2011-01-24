/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.util.Log;

/**
 * Helper class to send POST data over HTTP/HTTPS.
 */
public class HttpUtils {
	private static final String LOG_TAG = ACRA.LOG_TAG;

	private static final TrustManager[] TRUST_MANAGER = { new NaiveTrustManager() };

	private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();

	private static final int SOCKET_TIMEOUT = 3000;

	/**
	 * Send an HTTP(s) request with POST parameters.
	 * 
	 * @param parameters
	 * @param url
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public static void doPost(Map<?, ?> parameters, URL url, String login,
			String password) throws UnsupportedEncodingException, IOException,
			KeyManagementException, NoSuchAlgorithmException {

		URLConnection cnx = getConnection(url);

		// Construct data
		StringBuilder dataBfr = new StringBuilder();
		Iterator<?> iKeys = parameters.keySet().iterator();
		while (iKeys.hasNext()) {
			if (dataBfr.length() != 0) {
				dataBfr.append('&');
			}
			String key = (String) iKeys.next();
			dataBfr.append(URLEncoder.encode(key, "UTF-8"))
					.append('=')
					.append(URLEncoder.encode((String) parameters.get(key),
							"UTF-8"));
		}

		// Add BASIC auth credentials if available
		if (!isNull(login) || !isNull(password)) {
			String userPassword = (login != null ? login : "") + ":"
					+ (password != null ? password : "");
			String encodedAuth = Base64.encodeToString(userPassword.getBytes(),
					Base64.DEFAULT);
			cnx.setRequestProperty("Authorization", "Basic " + encodedAuth);
		}
		// POST data
		cnx.setDoOutput(true);

		OutputStreamWriter wr = new OutputStreamWriter(cnx.getOutputStream());
		Log.d(LOG_TAG, "Posting crash report data");
		wr.write(dataBfr.toString());
		wr.flush();
		wr.close();

		Log.d(LOG_TAG, "Reading response");
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				cnx.getInputStream()));

		String line;
		int linecount = 0;
		while ((line = rd.readLine()) != null) {
			linecount++;
			if (linecount <= 2) {
				Log.d(LOG_TAG, line);
			}
		}
		rd.close();
	}

	private static boolean isNull(String aString) {
		return aString == null || aString == ReportsCrashes.NULL_VALUE;
	}

	/**
	 * Open an URL connection. If HTTPS, accepts any certificate even if not
	 * valid, and connects to any host name.
	 * 
	 * @param url
	 *            The destination URL, HTTP or HTTPS.
	 * @return The URLConnection.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private static URLConnection getConnection(URL url) throws IOException,
			NoSuchAlgorithmException, KeyManagementException {
		URLConnection conn = url.openConnection();
		if (conn instanceof HttpsURLConnection) {
			// Trust all certificates
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(new KeyManager[0], TRUST_MANAGER, new SecureRandom());
			SSLSocketFactory socketFactory = context.getSocketFactory();
			((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);

			// Allow all hostnames
			((HttpsURLConnection) conn).setHostnameVerifier(HOSTNAME_VERIFIER);

		}
		conn.setConnectTimeout(SOCKET_TIMEOUT);
		conn.setReadTimeout(SOCKET_TIMEOUT);
		return conn;
	}

}
