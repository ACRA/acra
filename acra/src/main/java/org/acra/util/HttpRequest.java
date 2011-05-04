package org.acra.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpRequest {

	DefaultHttpClient httpClient;
	HttpContext localContext;
	private String ret;

	HttpResponse response = null;
	HttpPost httpPost = null;
	HttpGet httpGet = null;
	UsernamePasswordCredentials creds = null;

	public HttpRequest(String login, String password) {
		if (login != null || password != null) {
			creds = new UsernamePasswordCredentials(login, password);
		}
		HttpParams httpParams = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParams, ACRA.getConfig()
				.socketTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, ACRA.getConfig()
				.socketTimeout());
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), 80));
		registry.register(new Scheme("https", (new FakeSocketFactory()), 443));
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				httpParams, registry), httpParams);
		localContext = new BasicHttpContext();
	}

	public void clearCookies() {
		httpClient.getCookieStore().clear();
	}

	public void abort() {
		try {
			if (httpClient != null) {
				Log.d(ACRA.LOG_TAG, "Abort HttpClient request.");
				httpPost.abort();
			}
		} catch (Exception e) {
			Log.e(ACRA.LOG_TAG, "Error while aborting HttpClient request", e);
		}
	}

	public String sendPost(String url, String data)
			throws ClientProtocolException, IOException {
		return sendPost(url, data, null);
	}

	public String sendPost(String url, String data, String contentType)
			throws ClientProtocolException, IOException {
		ret = null;

		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);

		httpPost = new HttpPost(url);
		response = null;

		StringEntity tmp = null;

		Log.d(ACRA.LOG_TAG, "Setting httpPost headers");
		if (creds != null) {
			httpPost.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
		}
		httpPost.setHeader("User-Agent", "Android");
		httpPost.setHeader(
				"Accept",
				"text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

		if (contentType != null) {
			httpPost.setHeader("Content-Type", contentType);
		} else {
			httpPost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");
		}

		tmp = new StringEntity(data, "UTF-8");
		httpPost.setEntity(tmp);

		Log.d(ACRA.LOG_TAG, "Sending request to " + url);

		response = httpClient.execute(httpPost, localContext);

		if (response != null) {
			ret = EntityUtils.toString(response.getEntity());
		}

		Log.d(ACRA.LOG_TAG, "Returning value:" + ret);

		return ret;
	}

	public String sendGet(String url) throws ClientProtocolException, IOException {
		httpGet = new HttpGet(url);

		response = httpClient.execute(httpGet);

		// int status = response.getStatusLine().getStatusCode();

		// we assume that the response body contains the error message
		ret = EntityUtils.toString(response.getEntity());

		return ret;
	}

	public InputStream getHttpStream(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception e) {
			throw new IOException("Error connecting");
		} // end try-catch

		return in;
	}
}