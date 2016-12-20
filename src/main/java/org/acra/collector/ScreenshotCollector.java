/*
 * Copyright (c) 2016
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

package org.acra.collector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.View;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.LastActivityManager;
import org.acra.builder.ReportBuilder;

import java.io.ByteArrayOutputStream;

/**
 * @author F43nd1r
 * @since 19.11.2016
 */

public class ScreenshotCollector extends Collector {

    private final LastActivityManager lastActivityManager;

    public ScreenshotCollector(LastActivityManager lastActivityManager) {
        super(ReportField.SCREENSHOT);
        this.lastActivityManager = lastActivityManager;
    }

    @NonNull
    @Override
    String collect(ReportField reportField, ReportBuilder reportBuilder) {
        Activity activity = lastActivityManager.getLastActivity();
        if (activity != null) {
            try {
                View view = activity.getWindow().getDecorView();
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                view.draw(canvas);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            }catch (Exception e){
                //failed to print screen, return N/A
            }
        }
        return ACRAConstants.NOT_AVAILABLE;
    }
}
