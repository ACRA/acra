/*
 *  Copyright 2017
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
package org.acra.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import org.acra.ACRA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 09.03.2017
 */
public final class InstanceCreator {

    /**
     * Create an instance of clazz
     *
     * @param clazz    the clazz to create an instance of
     * @param fallback a value provider which provides a fallback in case of a failure
     * @param <T>      the return type
     * @return a new instance of clazz or fallback
     */
    @NonNull
    public <T> T create(@NonNull Class<? extends T> clazz, @NonNull Fallback<T> fallback) {
        T t = create(clazz);
        return t != null ? t : fallback.get();
    }

    @VisibleForTesting
    @Nullable
    <T> T create(@NonNull Class<? extends T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            ACRA.log.e(LOG_TAG, "Failed to create instance of class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            ACRA.log.e(LOG_TAG, "Failed to create instance of class " + clazz.getName(), e);
        }
        return null;
    }

    /**
     * Create instances of the given classes
     *
     * @param classes the classes to create insatnces of
     * @param <T>     the return type
     * @return a list of successfully created instances, does not contain null
     */
    @NonNull
    public <T> List<T> create(@NonNull Collection<Class<? extends T>> classes) {
        final List<T> result = new ArrayList<>();
        for (Class<? extends T> clazz : classes) {
            final T instance = create(clazz);
            if (instance != null) {
                result.add(instance);
            }
        }
        return result;
    }

    @FunctionalInterface
    public interface Fallback<T> {
        @NonNull
        T get();
    }
}
