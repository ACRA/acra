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
