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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.acra.ACRA.LOG_TAG;

/**
 * KeyStoreFactory for a certificate stored in a file
 *
 * @author F43nd1r
 * @since 4.8.3
 */
final class FileKeyStoreFactory extends BaseKeyStoreFactory {

    private final String filePath;

    /**
     * creates a new KeyStoreFactory for the specified file with a custom certificate type
     * @param certificateType the certificate type
     * @param filePath path to the file
     */
    FileKeyStoreFactory(String certificateType, String filePath) {
        super(certificateType);
        this.filePath = filePath;
    }

    @Override
    public InputStream getInputStream(@NonNull Context context) {
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            ACRA.log.e(LOG_TAG, "Could not find File "+filePath, e);
        }
        return null;
    }
}
