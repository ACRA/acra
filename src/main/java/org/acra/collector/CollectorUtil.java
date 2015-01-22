package org.acra.collector;

import java.io.IOException;
import java.io.Reader;

public final class CollectorUtil {

    /**
     * Closes a Reader.
     *
     * @param reader    Reader to close. If reader is null then method just returns.
     */
    public static void safeClose(Reader reader) {
    	if (reader == null) return;
    	
        try {
            reader.close();
        } catch (IOException e) {
            // We made out best effort to release this resource. Nothing more we can do.
        }
    }
}
