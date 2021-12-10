/*
 * Copyright (c) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.acra.config.CoreConfigurationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author F43nd1r
 * @since 30.11.2017
 */
@RunWith(AndroidJUnit4.class)
public class DefaultAttachmentProviderTest {
    @Test
    public void getAttachments() throws Exception {
        Uri uri = Uri.parse("content://not-a-valid-content-uri");
        List<Uri> result = new DefaultAttachmentProvider().getAttachments(ApplicationProvider.getApplicationContext(), new CoreConfigurationBuilder().withAttachmentUris(uri.toString()).build());
        assertThat(result, hasSize(1));
        assertEquals(uri, result.get(0));
    }

}