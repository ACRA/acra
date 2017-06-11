package org.acra.attachment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;

import java.util.List;

/**
 * @author F43nd1r
 * @since 09.03.2017
 */

public interface AttachmentUriProvider {

    @NonNull
    List<Uri> getAttachments(Context context, CoreConfiguration configuration);
}
