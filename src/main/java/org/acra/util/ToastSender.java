package org.acra.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import org.acra.ACRA;

import static org.acra.ACRA.LOG_TAG;

/**
 * Responsible for sending Toasts under all circumstances.
 * <p/>
 * @author William Ferguson
 * @since 4.3.0
 */
public final class ToastSender {
    private ToastSender(){}

    /**
     * Sends a Toast and ensures that any Exception thrown during sending is handled.
     *
     * @param context           Application context.
     * @param toastResourceId   Id of the resource to send as the Toast message.
     * @param toastLength       Length of the Toast.
     */
    public static void sendToast(Context context, @StringRes int toastResourceId, int toastLength) {
        try {
            Toast.makeText(context, toastResourceId, toastLength).show();
        } catch (RuntimeException e) {
            ACRA.log.w(LOG_TAG, "Could not send crash Toast", e);
        }
    }
}
