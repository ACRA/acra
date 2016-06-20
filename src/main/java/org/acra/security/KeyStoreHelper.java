/*
 *  Copyright 2016
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
package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.ACRAConfiguration;

import java.security.KeyStore;

import static org.acra.ACRA.LOG_TAG;

/**
 * Helper to get a KeyStore from a configuration
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public final class KeyStoreHelper {
    private static final String ASSET_PREFIX = "asset://";

    private KeyStoreHelper() {
    }

    /**
     * try to get the keystore
     * @param context a context
     * @param config the configuration
     * @return the keystore, or null if none provided / failure
     */
    @Nullable
    public static KeyStore getKeyStore(@NonNull Context context, @NonNull ACRAConfiguration config) {
        final Class<? extends KeyStoreFactory> keyStoreFactory = config.keyStoreFactoryClass();
        KeyStore keyStore = null;
        try {
            keyStore = keyStoreFactory.newInstance().create(context);
        } catch (InstantiationException e) {
            ACRA.log.e(LOG_TAG, "Could not get keystore from factory", e);
        } catch (IllegalAccessException e) {
            ACRA.log.e(LOG_TAG, "Could not get keystore from factory", e);
        }
        if(keyStore == null) {
            //either users factory did not create a keystore, or the configuration is default {@link NoKeyStoreFactory}
            final int certificateRes = config.resCertificate();
            final String certificatePath = config.certificatePath();
            final String certificateType = config.certificateType();
            if(certificateRes != ACRAConstants.DEFAULT_RES_VALUE){
                keyStore = new ResourceKeyStoreFactory(certificateType, certificateRes).create(context);
            }else if(!certificatePath.equals(ACRAConstants.DEFAULT_STRING_VALUE)){
                if(certificatePath.startsWith(ASSET_PREFIX)) {
                    keyStore = new AssetKeyStoreFactory(certificateType, certificatePath.substring(ASSET_PREFIX.length())).create(context);
                } else {
                    keyStore = new FileKeyStoreFactory(certificateType, certificatePath).create(context);
                }
            }
        }
        return keyStore;
    }
}
