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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.test.ProviderTestCase2;
import android.webkit.MimeTypeMap;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.acra.ACRA;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author F43nd1r
 * @since 04.12.2017
 */
@Ignore
@RunWith(AndroidJUnit4.class)
public class AcraContentProviderTest extends ProviderTestCase2<AcraContentProvider> {
    private static final String JSON_EXTENSION = "json";
    private static final String JSON_MIMETYPE = "application/json";

    private ContentResolver resolver;
    private File file;

    public AcraContentProviderTest() {
        super(AcraContentProvider.class, ApplicationProvider.getApplicationContext().getPackageName() + ".acra");
    }

    @Before
    public void setUp() throws Exception {
        ACRA.DEV_LOGGING = true;
        resolver = ApplicationProvider.getApplicationContext().getContentResolver();
        file = File.createTempFile("test", "." + JSON_EXTENSION);
    }

    @Test
    public void query() {
        final Cursor cursor = resolver.query(AcraContentProvider.getUriForFile(ApplicationProvider.getApplicationContext(), file), new String[]{OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(file.length(), cursor.getInt(0));
        assertEquals(file.getName(), cursor.getString(1));
        assertFalse(cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void openFile() throws Exception {
        final Uri uri = AcraContentProvider.getUriForFile(ApplicationProvider.getApplicationContext(), file);
        assertNotNull(resolver.openFileDescriptor(uri, "r"));

    }

    @Test
    public void guessMimeType() {
        assertEquals(JSON_MIMETYPE, AcraContentProvider.guessMimeType(AcraContentProvider.getUriForFile(ApplicationProvider.getApplicationContext(), file)));
    }

}