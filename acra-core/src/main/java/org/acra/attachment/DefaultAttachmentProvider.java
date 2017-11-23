/*
 * Copyright (c) 2017 the ACRA team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
