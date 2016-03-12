package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import java.io.InputStream;

/**
 * KeyStoreFactory for a certificate stored in a raw resource
 */
public class ResourceKeyStoreFactory extends BaseKeyStoreFactory {

    @RawRes
    private final int rawRes;

    public ResourceKeyStoreFactory(@RawRes int rawRes) {
        super();
        this.rawRes = rawRes;
    }

    public ResourceKeyStoreFactory(String certificateType, @RawRes int rawRes) {
        super(certificateType);
        this.rawRes = rawRes;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        return context.getResources().openRawResource(rawRes);
    }
}
