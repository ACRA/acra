/*
 * Copyright (c) 2017 the ACRA team
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

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.acra.ACRA;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for sending Toasts under all circumstances.
 *
 * @author William Ferguson
 * @since 4.3.0
 */
public final class ToastSender {
    private ToastSender() {
    }

    /**
     * Sends a Toast and ensures that any Exception thrown during sending is handled.
     *
     * @param context     Application context.
     * @param toast       toast message.
     * @param toastLength Length of the Toast.
     */
    public static void sendToast(@NonNull Context context, String toast, @IntRange(from = 0, to = 1) int toastLength) {
        try {
            Toast.makeText(context, toast, toastLength).show();
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Could not send crash Toast", e);
        }
    }
}
