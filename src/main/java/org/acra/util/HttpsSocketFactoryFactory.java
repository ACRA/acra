package org.acra.util;

import android.content.Context;
import org.apache.http.conn.scheme.SocketFactory;

/**
 * Factory that creates an instance of a Https SocketFactory.
 */
public interface HttpsSocketFactoryFactory {

    /**
     * @param context   Android context for which to create the SocketFactory.
     * @return SocketFactory that was created.
     */
    public SocketFactory create(Context context);
}
