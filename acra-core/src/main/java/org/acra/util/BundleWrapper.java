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
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Works like a {@link PersistableBundle}, but falls back to {@link Bundle} on older versions
 *
 * @author lukas
 * @since 29.11.19
 */
@Keep
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

    @Keep
    interface Internal extends BundleWrapper {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        PersistableBundle asPersistableBundle();

        /**
         * Only works on API &lt; 22
         *
         * @return this as bundle
         */
        Bundle asBundle();
    }

    static BundleWrapper wrap(@Nullable Bundle bundle) {
        BundleWrapper wrapper = create();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object o = bundle.get(key);
                if (o instanceof Integer) {
                    wrapper.putInt(key, (Integer) o);
                } else if (o instanceof int[]) {
                    wrapper.putIntArray(key, (int[]) o);
                } else if (o instanceof Long) {
                    wrapper.putLong(key, (Long) o);
                } else if (o instanceof long[]) {
                    wrapper.putLongArray(key, (long[]) o);
                } else if (o instanceof Double) {
                    wrapper.putDouble(key, (Double) o);
                } else if (o instanceof double[]) {
                    wrapper.putDoubleArray(key, (double[]) o);
                } else if (o instanceof String) {
                    wrapper.putString(key, (String) o);
                } else if (o instanceof String[]) {
                    wrapper.putStringArray(key, (String[]) o);
                } else if (o instanceof Boolean) {
                    wrapper.putBoolean(key, (Boolean) o);
                } else if (o instanceof boolean[]) {
                    wrapper.putBooleanArray(key, (boolean[]) o);
                }
            }
        }
        return wrapper;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static BundleWrapper wrap(@Nullable PersistableBundle bundle) {
        BundleWrapper wrapper = create();
        if (bundle != null) {
            wrapper.putAll(bundle);
        }
        return wrapper;
    }

    static BundleWrapper.Internal create() {
        final Object wrap = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? new PersistableBundle() : new Bundle();
        return (BundleWrapper.Internal) Proxy.newProxyInstance(BundleWrapper.class.getClassLoader(), new Class[]{BundleWrapper.Internal.class}, (proxy, method, args) -> {
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
            return wrap.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(wrap, args);
        });
    }
}
