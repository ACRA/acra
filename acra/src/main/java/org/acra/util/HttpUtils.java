/*
 *  Copyright 2010 Kevin Gaudin
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

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.acra.ACRA;

/**
 * Helper class to send POST data over HTTP/HTTPS.
 */
public class HttpUtils {

	/**
	 * Send an HTTP(s) request with POST parameters.
	 * 
	 * @param parameters    Map of parameters to post to a URL.
	 * @param url           URL to which to post to.
     * @param login         Username to supply as credentials in the post. May be null.
     * @param password      Password to supply as credentials in the post. May be null.
	 * @throws IOException if the data cannot be posted.
	 */
	public static void doPost(Map<?, ?> parameters, URL url, String login, String password) throws IOException {

		// Construct data
		final StringBuilder dataBfr = new StringBuilder();
		for (final Object key : parameters.keySet()) {
			if (dataBfr.length() != 0) {
				dataBfr.append('&');
			}
			final Object preliminaryValue = parameters.get(key);
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
			dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
		}

        final String vettedLogin = isNull(login) ? null : login;
        final String vettedPassword = isNull(password) ? null : password;

		final HttpRequest req = new HttpRequest(vettedLogin, vettedPassword);
		req.sendPost(url.toString(), dataBfr.toString());
	}

	private static boolean isNull(String aString) {
		return aString == null || ACRA.NULL_VALUE.equals(aString);
	}
}
