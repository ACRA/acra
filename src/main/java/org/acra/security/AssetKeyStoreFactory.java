package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.ACRA;

import java.io.IOException;
import java.io.InputStream;

import static org.acra.ACRA.LOG_TAG;

/**
 * KeyStoreFactory for a certificate stored in an asset file
 */
public class AssetKeyStoreFactory extends BaseKeyStoreFactory {

    private final String assetName;

    public AssetKeyStoreFactory(String assetName) {
        super();
        this.assetName = assetName;
    }

    public AssetKeyStoreFactory(String certificateType, String assetName) {
        super(certificateType);
        this.assetName = assetName;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        try {
            return context.getAssets().open(assetName);
        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "", e);
        }
        return null;
    }
}
