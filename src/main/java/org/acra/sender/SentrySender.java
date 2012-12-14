package org.acra.sender;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender.Method;
import org.acra.util.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;


public class SentrySender implements ReportSender {

	private SentryConfig config;
    public static final ReportField[] SENTRY_TAGS_FIELDS = { APP_VERSION_CODE, APP_VERSION_NAME,
        PACKAGE_NAME, FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION, TOTAL_MEM_SIZE,
        AVAILABLE_MEM_SIZE, IS_SILENT, USER_APP_START_DATE, USER_CRASH_DATE, INSTALLATION_ID };
	/**
	 * Takes in a sentryDSN
	 *
	 * @param sentryDSN '{PROTOCOL}://{PUBLIC_KEY}:{SECRET_KEY}@{HOST}/{PATH}/{PROJECT_ID}'
	 */
	public SentrySender(String sentryDSN) {
		if (sentryDSN == null) {
			return;
		}
		config = new SentryConfig(sentryDSN);
	}
	public SentrySender(){
		if (ACRA.getConfig().formKey() == null) {
			return;
		}
		config = new SentryConfig( ACRA.getConfig().formKey() );
	}
	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException {
		
		if (config == null) {
			return;
		}
		
		final HttpRequest request = new HttpRequest();
		request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
		request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
		request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());
		request.extra_headers.put("X-Sentry-Auth", buildAuthHeader());
		try {
			request.send(config.getSentryURL(), Method.POST, buildJSON(errorContent), org.acra.sender.HttpSender.Type.JSON);
		} catch (MalformedURLException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		} catch (IOException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		} catch (JSONException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		}
	}
	  /**
     * Build up the sentry auth header in the following format.
     * <p/>
     * The header is composed of the timestamp from when the message was generated, and an
     * arbitrary client version string. The client version should be something distinct to your client,
     * and is simply for reporting purposes.
     * <p/>
     * X-Sentry-Auth: Sentry sentry_version=3,
     * sentry_timestamp=<signature timestamp>[,
     * sentry_key=<public api key>,[
     * sentry_client=<client version, arbitrary>]]
     *
     * @param hmacSignature SHA1-signed HMAC
     * @param publicKey     is either the public_key or the shared global key between client and server.
     * @return String version of the sentry auth header
     */
    protected String buildAuthHeader() {
/*    	X-Sentry-Auth: Sentry sentry_version=3,
    			sentry_client=<client version, arbitrary>,
    			sentry_timestamp=<current timestamp>,
    			sentry_key=<public api key>,
    			sentry_secret=<secret api key>*/
        StringBuilder header = new StringBuilder();
        header.append("Sentry sentry_version=3");
        header.append(",sentry_client=ACRA");
        header.append(",sentry_timestamp=");
        header.append(new Date().getTime());
        header.append(",sentry_key=");
        header.append(config.getPublicKey());
        header.append(",sentry_secret=");
        header.append(config.getSecretKey());

        return header.toString();
    }

	/**
	 * Given the time right now return a ISO8601 formatted date string
	 *
	 * @return ISO8601 formatted date string
	 */
	public String getTimestampString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		return df.format(new Date());
	}

	private String buildJSON(CrashReportData report) throws JSONException {
		JSONObject obj = new JSONObject();
		String message = report.getProperty(ReportField.STACK_TRACE).split("\n")[0];
		obj.put("event_id", report.getProperty(ReportField.REPORT_ID)); //Hexadecimal string representing a uuid4 value.
		
		obj.put("culprit", report.getProperty(ReportField.FILE_PATH));
		obj.put("sentry.interfaces.Stacktrace", report.getProperty(ReportField.STACK_TRACE));
		
		obj.put("level", "error");
		obj.put("timestamp", getTimestampString());
		obj.put("message", message);

		obj.put("logger", "org.acra");
		obj.put("platform", "android");
		obj.put("tags", remap(report, SENTRY_TAGS_FIELDS));
		if (ACRA.getConfig().customReportContent().length > 0) {
			obj.put("extra", remap(report, ACRA.getConfig().customReportContent()));
		}
		
		ACRA.log.d(ACRA.LOG_TAG, obj.toString());
		
		return obj.toString();
	}
 
	private JSONObject remap(CrashReportData report, ReportField[] fields) throws JSONException {

        final JSONObject result = new JSONObject(); 
        for (ReportField originalKey : fields) {
        	result.put(originalKey.toString(), report.getProperty(originalKey));
        	ACRA.log.d(ACRA.LOG_TAG, originalKey.toString() + ": "+ report.getProperty(originalKey));
        }
        return result;
    }
        
	private class SentryConfig {

		private String host, protocol, publicKey, secretKey, path, projectId;
		private int port;

		/**
		 * Takes in a sentryDSN and builds up the configuration
		 *
		 * @param sentryDSN '{PROTOCOL}://{PUBLIC_KEY}:{SECRET_KEY}@{HOST}/{PATH}/{PROJECT_ID}'
		 */
		public SentryConfig(String sentryDSN) {

			try {
				URL url = new URL(sentryDSN);
				this.host = url.getHost();
				this.protocol = url.getProtocol();
				String urlPath = url.getPath();

				int lastSlash = urlPath.lastIndexOf("/");
				this.path = urlPath.substring(0, lastSlash);
				// ProjectId is the integer after the last slash in the path
				this.projectId = urlPath.substring(lastSlash + 1);

				String userInfo = url.getUserInfo();
				String[] userParts = userInfo.split(":");

				this.secretKey = userParts[1];
				this.publicKey = userParts[0];

				this.port = url.getPort();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}

		/**
		 * The Sentry server URL that we post the message to.
		 *
		 * @return sentry server url
		 * @throws MalformedURLException 
		 */
		public URL getSentryURL() throws MalformedURLException {
			StringBuilder serverUrl = new StringBuilder();
			serverUrl.append(getProtocol());
			serverUrl.append("://");
			serverUrl.append(getHost());
			if ((getPort() != 0) && (getPort() != 80) && getPort() != -1) {
				serverUrl.append(":").append(getPort());
			}
			serverUrl.append(getPath());
			serverUrl.append("/api/store/");
			return new URL(serverUrl.toString());
		}

		/**
		 * The sentry server host
		 *
		 * @return server host
		 */
		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		/**
		 * Sentry server protocol http https?
		 *
		 * @return http or https
		 */
		public String getProtocol() {
			return protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		/**
		 * The Sentry public key
		 *
		 * @return Sentry public key
		 */
		public String getPublicKey() {
			return publicKey;
		}

		public void setPublicKey(String publicKey) {
			this.publicKey = publicKey;
		}

		/**
		 * The Sentry secret key
		 *
		 * @return Sentry secret key
		 */
		public String getSecretKey() {
			return secretKey;
		}

		public void setSecretKey(String secretKey) {
			this.secretKey = secretKey;
		}

		/**
		 * sentry url path
		 *
		 * @return url path
		 */
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * Sentry project Id
		 *
		 * @return project Id
		 */
		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		/**
		 * sentry server port
		 *
		 * @return server port
		 */
		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}
}

