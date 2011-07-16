package org.acra.util;

import org.acra.ACRA;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Responsible for sending Toasts under all circumstances.
 * <p/>
 * User: William
 * Date: 16/07/11
 * Time: 4:29 PM
 */
public final class ToastSender {

    /**
     * Sends a Toast and ensures that any Exception thrown during sending is handled.
     *
     * @param context           Application context.
     * @param toastResourceId   Id of the resource to send as the Toast message.
     * @param toastLength       Length of the Toast.
     */
    public static void sendToast(Context context, int toastResourceId, int toastLength) {
        try {
            Toast.makeText(context, toastResourceId, toastLength).show();
        } catch (RuntimeException e) {
            Log.e(ACRA.LOG_TAG, "Could not send crash Toast", e);
        }
    }
}
