package org.acra.attachment;

import android.content.Context;
import android.net.Uri;

import org.acra.config.ACRAConfiguration;

import java.util.ArrayList;

/**
 * @author F43nd1r
 * @since 10.03.2017
 */

public class NoAttachmentUriProvider implements AttachmentUriProvider {
    @Override
    public ArrayList<Uri> getAttachments(Context context, ACRAConfiguration configuration) {
        return new ArrayList<Uri>();
    }
}
