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

package org.acra.data;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.acra.ReportField;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

/**
 * @author F43nd1r
 * @since 29.11.2017
 */
@RunWith(AndroidJUnit4.class)
public class StringFormatTest {
    private CrashReportData reportData;

    @Before
    public void setUp() throws Exception {
        reportData = new CrashReportData();
        reportData.put(ReportField.DEVICE_ID, "FAKE_ID");
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("VERSION_CODE", -1);
        jsonObject.put("VERSION_NAME", "Test");
        reportData.put(ReportField.BUILD_CONFIG, jsonObject);
    }

    @Test
    public void toFormattedString() throws Exception {
        testJson();
        testKeyValue();
    }


    public void testJson() throws Exception {
        assertEquals("{\"DEVICE_ID\":\"FAKE_ID\",\"BUILD_CONFIG\":{\"VERSION_CODE\":-1,\"VERSION_NAME\":\"Test\"}}",
                StringFormat.JSON.toFormattedString(reportData, Arrays.asList(ReportField.DEVICE_ID, ReportField.BUILD_CONFIG), "\n", " ", false));
        assertEquals("{\"DEVICE_ID\":\"FAKE_ID\",\"BUILD_CONFIG\":{\"VERSION_CODE\":-1,\"VERSION_NAME\":\"Test\"}}",
                StringFormat.JSON.toFormattedString(reportData, Arrays.asList(ReportField.DEVICE_ID, ReportField.BUILD_CONFIG), "&", "\n", true));
    }

    public void testKeyValue() throws Exception {
        assertEquals("DEVICE_ID=FAKE_ID\nBUILD_CONFIG=VERSION_CODE=-1 VERSION_NAME=Test",
                StringFormat.KEY_VALUE_LIST.toFormattedString(reportData, Arrays.asList(ReportField.DEVICE_ID, ReportField.BUILD_CONFIG), "\n", " ", false));
        assertEquals("DEVICE_ID=FAKE_ID&BUILD_CONFIG=VERSION_CODE%3D-1%0AVERSION_NAME%3DTest",
                StringFormat.KEY_VALUE_LIST.toFormattedString(reportData, Arrays.asList(ReportField.DEVICE_ID, ReportField.BUILD_CONFIG), "&", "\n", true));
    }

    @Test
    public void getMatchingHttpContentType() {
        assertEquals("application/json", StringFormat.JSON.getMatchingHttpContentType());
        assertEquals("application/x-www-form-urlencoded", StringFormat.KEY_VALUE_LIST.getMatchingHttpContentType());
    }

    @Test
    public void issue626() throws Exception {
        CrashReportData reportData = new CrashReportData();
        assertEquals("DEVICE_ID=null", StringFormat.KEY_VALUE_LIST.toFormattedString(reportData, Arrays.asList(ReportField.DEVICE_ID), "\n", " ", true));
    }
}