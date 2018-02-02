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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import org.acra.ACRA;
import org.acra.log.RobolectricLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author F43nd1r
 * @since 04.12.2017
 */
@RunWith(RobolectricTestRunner.class)
public class AcraContentProviderTest {
    private static final String JSON_EXTENSION = "json";
    private static final String JSON_MIMETYPE = MediaType.JSON_UTF_8.type() + "/" + MediaType.JSON_UTF_8.subtype();

    private ContentResolver resolver;
    private File file;

    @Before
    public void setUp() throws Exception {
        ACRA.log = new RobolectricLog();
        ACRA.DEV_LOGGING = true;
        Robolectric.setupContentProvider(AcraContentProvider.class, RuntimeEnvironment.application.getPackageName() + ".acra");
        resolver = RuntimeEnvironment.application.getContentResolver();
        file = File.createTempFile("test", "." + JSON_EXTENSION);
        Shadows.shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping(JSON_EXTENSION, JSON_MIMETYPE);
    }

    @Test
    public void query() {
        final Cursor cursor = resolver.query(AcraContentProvider.getUriForFile(RuntimeEnvironment.application, file), new String[]{OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(file.length(), cursor.getInt(0));
        assertEquals(file.getName(), cursor.getString(1));
        assertFalse(cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void openFile() throws Exception {
        final Uri uri = AcraContentProvider.getUriForFile(RuntimeEnvironment.application, file);
        assertNotNull(resolver.openFileDescriptor(uri, "r"));

    }

    @Test
    public void guessMimeType() {
        assertEquals(JSON_MIMETYPE, AcraContentProvider.guessMimeType(AcraContentProvider.getUriForFile(RuntimeEnvironment.application, file)));
    }

}