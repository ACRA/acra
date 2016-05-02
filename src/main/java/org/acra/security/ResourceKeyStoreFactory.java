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
import android.support.annotation.RawRes;

import java.io.InputStream;

/**
 * KeyStoreFactory for a certificate stored in a raw resource
 *
 * @author F43nd1r
 * @since 4.8.3
 */
final class ResourceKeyStoreFactory extends BaseKeyStoreFactory {

    @RawRes
    private final int rawRes;

    /**
     * creates a new KeyStoreFactory for the specified resource with a custom certificate type
     * @param certificateType the certificate type
     * @param rawRes raw resource id
     */
    ResourceKeyStoreFactory(String certificateType, @RawRes int rawRes) {
        super(certificateType);
        this.rawRes = rawRes;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        return context.getResources().openRawResource(rawRes);
    }
}
