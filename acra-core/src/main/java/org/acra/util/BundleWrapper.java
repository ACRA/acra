/*
 * Copyright (c) 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.util;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * @author lukas
 * @since 29.11.19
 */
public interface BundleWrapper {

    int size();

    boolean isEmpty();

    void clear();

    boolean containsKey(String key);

    @Nullable
    Object get(String key);

    void remove(String key);


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void putAll(PersistableBundle bundle);

    Set<String> keySet();

    void putBoolean(@Nullable String key, boolean value);

    void putInt(@Nullable String key, int value);

    void putLong(@Nullable String key, long value);

    void putDouble(@Nullable String key, double value);

    void putString(@Nullable String key,
                   @Nullable String value);

    void putBooleanArray(@Nullable String key,
                         @Nullable boolean[] value);

    void putIntArray(@Nullable String key,
                     @Nullable int[] value);

    void putLongArray(@Nullable String key,
                      @Nullable long[] value);

    void putDoubleArray(@Nullable String key,
                        @Nullable double[] value);

    void putStringArray(@Nullable String key,
                        @Nullable String[] value);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    int getInt(String key);

    int getInt(String key, int defaultValue);

    long getLong(String key);

    long getLong(String key, long defaultValue);

    double getDouble(String key);

    double getDouble(String key, double defaultValue);

    @Nullable
    String getString(@Nullable String key);

    String getString(@Nullable String key, String defaultValue);

    @Nullable
    boolean[] getBooleanArray(@Nullable String key);

    @Nullable
    int[] getIntArray(@Nullable String key);

    @Nullable
    long[] getLongArray(@Nullable String key);

    @Nullable
    double[] getDoubleArray(@Nullable String key);

    @Nullable
    String[] getStringArray(@Nullable String key);

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    PersistableBundle asPersistableBundle();

    /**
     * Only works on API < 22
     *
     * @return this as bundle
     */
    Bundle asBundle();

    static BundleWrapper create() {
        Object wrap = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? new PersistableBundle() : new Bundle();
        return (BundleWrapper) Proxy.newProxyInstance(BundleWrapper.class.getClassLoader(), new Class[]{BundleWrapper.class}, (proxy, method, args) -> {
            if (method.getName().equals("asPersistableBundle")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    return wrap;
                }
                return null;
            }
            if (method.getName().equals("asBundle")) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                    return wrap;
                }
                return null;
            }
            return method.invoke(wrap, args);
        });
    }
}
