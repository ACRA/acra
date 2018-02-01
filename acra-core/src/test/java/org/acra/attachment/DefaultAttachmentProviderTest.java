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

import android.app.Application;
import android.net.Uri;

import org.acra.config.CoreConfigurationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author F43nd1r
 * @since 30.11.2017
 */
@RunWith(RobolectricTestRunner.class)
public class DefaultAttachmentProviderTest {
    @Test
    public void getAttachments() throws Exception {
        Uri uri = Uri.parse("content://not-a-valid-content-uri");
        List<Uri> result = new DefaultAttachmentProvider().getAttachments(RuntimeEnvironment.application, new CoreConfigurationBuilder(new Application()).setAttachmentUris(uri.toString()).build());
        assertThat(result, hasSize(1));
        assertEquals(uri, result.get(0));
    }

}