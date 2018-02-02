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

package org.acra.data;

import org.acra.ReportField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author F43nd1r
 * @since 29.11.2017
 */
@RunWith(RobolectricTestRunner.class)
public class CrashReportDataTest {
    @Test
    public void put() {
        final CrashReportData data = new CrashReportData();
        data.put(ReportField.DEVICE_ID, "FAKE_ID");
        assertEquals("FAKE_ID", data.getString(ReportField.DEVICE_ID));
        data.put("CUSTOM","\n");
        assertEquals("\n", data.get("CUSTOM"));
        data.put(ReportField.DEVICE_ID, -1);
        assertEquals("-1", data.getString(ReportField.DEVICE_ID));
        assertEquals(-1, data.get(ReportField.DEVICE_ID.name()));
    }

    @Test
    public void containsKey() {
        final CrashReportData data = new CrashReportData();
        data.put(ReportField.DEVICE_ID, "FAKE_ID");
        data.put("CUSTOM","\n");
        assertTrue(data.containsKey(ReportField.DEVICE_ID));
        assertTrue(data.containsKey(ReportField.DEVICE_ID.name()));
        assertTrue(data.containsKey("CUSTOM"));
    }

    @Test
    public void toMap() {
        final CrashReportData data = new CrashReportData();
        data.put(ReportField.DEVICE_ID, "FAKE_ID");
        data.put("CUSTOM",-1);
        final Map<String, Object> map = data.toMap();
        assertEquals("FAKE_ID", map.get(ReportField.DEVICE_ID.name()));
        assertEquals(-1, map.get("CUSTOM"));
    }

}