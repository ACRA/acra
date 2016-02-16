package org.acra.collector;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.Reader;

public final class CollectorUtil {
    private CollectorUtil(){}

    /**
     * Closes a Reader.
     *
     * @param reader    Reader to close. If reader is null then method just returns.
     */
    public static void safeClose(@Nullable Reader reader) {
    	if (reader == null) return;
    	
        try {
            reader.close();
        } catch (IOException e) {
            // We made out best effort to release this resource. Nothing more we can do.
        }
    }
}
