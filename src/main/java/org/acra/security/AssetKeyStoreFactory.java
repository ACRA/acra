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

import org.acra.ACRA;

import java.io.IOException;
import java.io.InputStream;

import static org.acra.ACRA.LOG_TAG;

/**
 * KeyStoreFactory for a certificate stored in an asset file
 *
 * @author F43nd1r
 * @since 4.8.3
 */
final class AssetKeyStoreFactory extends BaseKeyStoreFactory {

    private final String assetName;

    /**
     * creates a new KeyStoreFactory for the specified asset with a custom certificate type
     * @param certificateType the certificate type
     * @param assetName the asset
     */
    AssetKeyStoreFactory(String certificateType, String assetName) {
        super(certificateType);
        this.assetName = assetName;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        try {
            return context.getAssets().open(assetName);
        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "Could not open certificate in asset://"+assetName, e);
        }
        return null;
    }
}
