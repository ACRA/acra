package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.acra.ACRA.LOG_TAG;

/**
 * Provides base KeyStoreFactory implementation
 */
public abstract class BaseKeyStoreFactory implements KeyStoreFactory {

    private final String certificateType;

    public BaseKeyStoreFactory(){
        this("X.509");
    }

    public BaseKeyStoreFactory(String certificateType) {
        this.certificateType = certificateType;
    }

    abstract public InputStream getInputStream(@NonNull Context context);

    @Override
    @Nullable
    public final KeyStore create(@NonNull Context context) {
        InputStream inputStream = getInputStream(context);
        if (inputStream != null) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance(certificateType);
                Certificate certificate = certificateFactory.generateCertificate(bufferedInputStream);
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);
                return keyStore;
            } catch (CertificateException e) {
                ACRA.log.e(LOG_TAG, "", e);
            } catch (KeyStoreException e) {
                ACRA.log.e(LOG_TAG, "", e);
            } catch (NoSuchAlgorithmException e) {
                ACRA.log.e(LOG_TAG, "", e);
            } catch (IOException e) {
                ACRA.log.e(LOG_TAG, "", e);
            } finally {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    ACRA.log.e(LOG_TAG, "", e);
                }
            }
        }
        return null;
    }
}
