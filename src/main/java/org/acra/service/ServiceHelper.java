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
package org.acra.service;

import android.app.Service;
import android.support.annotation.Nullable;

import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Helps to prevent infinite Service restarts
 *
 * @author F43nd1r
 * @since 4.9.1
 */
public final class ServiceHelper {

    private static final WeakHashMap<Thread, Class<? extends Service>> map = new WeakHashMap<Thread, Class<? extends Service>>();

    private final Class<? extends Service> service;

    public ServiceHelper(Class<? extends Service> service) {
        this.service = service;
    }

    public ThreadPoolExecutor getNewExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        executor.setThreadFactory(new RegisteredThreadFactory());
        return executor;
    }

    public void registerThread(Thread thread) {
        map.put(thread, service);
    }

    @Nullable
    public static Class<? extends Service> getOwner(Thread thread){
        return map.get(thread);
    }

    private class RegisteredThreadFactory implements ThreadFactory {
        private ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            registerThread(thread);
            return thread;
        }
    }
}
