package org.acra.attachment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Reads attachment uris from the configuration
 *
 * @author F43nd1r
 * @since 10.03.2017
 */

public class DefaultAttachmentProvider implements AttachmentUriProvider {

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<Uri> getAttachments(@NonNull Context context, @NonNull CoreConfiguration configuration) {
        final ArrayList<Uri> result = new ArrayList<>();
        for (String s : configuration.attachmentUris()) {
            try {
                result.add(Uri.parse(s));
            } catch (Exception e) {
                ACRA.log.e(LOG_TAG, "Failed to parse Uri " + s, e);
            }
        }
        return result;
    }
}
