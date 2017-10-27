package org.acra.attachment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;

import java.util.List;

/**
 * Provides attachment uris to ACRA
 *
 * @author F43nd1r
 * @since 09.03.2017
 */
public interface AttachmentUriProvider {

    /**
     * @param context       a context
     * @param configuration ACRA configuration
     * @return all file uris that should be attached to the report
     */
    @NonNull
    List<Uri> getAttachments(@NonNull Context context, @NonNull CoreConfiguration configuration);
}
