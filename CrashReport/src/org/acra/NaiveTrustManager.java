package org.acra;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class NaiveTrustManager implements X509TrustManager {
	public NaiveTrustManager() {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

	public void checkClientTrusted(X509Certificate[] x509CertificateArray,
			String string) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] x509CertificateArray,
			String string) throws CertificateException {
	}
}