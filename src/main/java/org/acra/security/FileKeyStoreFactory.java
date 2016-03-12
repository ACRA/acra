package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.acra.ACRA.LOG_TAG;

/**
 * KeyStoreFactory for a certificate stored in a file
 */
public class FileKeyStoreFactory extends BaseKeyStoreFactory {

    private final String filePath;

    /**
     * creates a new KeyStoreFactory for the specified file
     * @param filePath path to the file
     */
    public FileKeyStoreFactory(String filePath) {
        super();
        this.filePath = filePath;
    }

    /**
     * creates a new KeyStoreFactory for the specified file with a custom certificate type
     * @param certificateType the certificate type
     * @param filePath path to the file
     */
    public FileKeyStoreFactory(String certificateType, String filePath) {
        super(certificateType);
        this.filePath = filePath;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            ACRA.log.e(LOG_TAG, "", e);
        }
        return null;
    }
}
