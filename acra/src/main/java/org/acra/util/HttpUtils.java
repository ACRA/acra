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
import org.apache.http.client.ClientProtocolException;

/**
 * Helper class to send POST data over HTTP/HTTPS.
 */
public class HttpUtils {

	/**
	 * Send an HTTP(s) request with POST parameters.
	 * 
	 * @param parameters
	 * @param url
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static void doPost(Map<?, ?> parameters, URL url, String login,
			String password) throws ClientProtocolException, IOException {

		// Construct data
		StringBuilder dataBfr = new StringBuilder();
		for (Object key : parameters.keySet()) {
			if (dataBfr.length() != 0) {
				dataBfr.append('&');
			}
			Object value = parameters.get(key);
			if (value == null) {
				value = "";
			}
			dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"))
					.append('=')
					.append(URLEncoder.encode(value.toString(), "UTF-8"));
		}

		HttpRequest req = new HttpRequest(isNull(login) ? null : login,
				isNull(password) ? null : password);
		req.sendPost(url.toString(), dataBfr.toString());
	}

	private static boolean isNull(String aString) {
		return aString == null || aString == ACRA.NULL_VALUE;
	}

}
