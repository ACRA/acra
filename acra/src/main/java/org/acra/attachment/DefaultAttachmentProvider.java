package org.acra.attachment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;

import java.util.ArrayList;

import static org.acra.ACRA.LOG_TAG;

/**
 * @author F43nd1r
 * @since 10.03.2017
 */

public class DefaultAttachmentProvider implements AttachmentUriProvider {
    @NonNull
    @Override
    public ArrayList<Uri> getAttachments(Context context, ACRAConfiguration configuration) {
        final ArrayList<Uri> result = new ArrayList<Uri>();
        for (String s : configuration.attachmentUris()){
            try {
                result.add(Uri.parse(s));
            }catch (Exception e){
                ACRA.log.e(LOG_TAG, "Failed to parse Uri " + s, e);
            }
        }
        return result;
    }
}
