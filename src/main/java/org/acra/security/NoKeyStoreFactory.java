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

import java.security.KeyStore;

/**
 * Default KeyStoreFactory. Does not provide any KeyStore
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public class NoKeyStoreFactory implements KeyStoreFactory {
    @Nullable
    @Override
    public KeyStore create(@NonNull Context context) {
        return null;
    }
}
