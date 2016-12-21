/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acra.collector;

import android.support.annotation.NonNull;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.model.ComplexElement;
import org.acra.model.Element;
import org.json.JSONException;

/**
 * Collects some data identifying a Thread
 *
 * @author Kevin Gaudin & F43nd1r
 */
final class ThreadCollector extends Collector {
    ThreadCollector() {
        super(ReportField.THREAD_DETAILS);
    }

    /**
     * collects some data identifying the crashed thread
     *
     * @return the information including the id, name and priority of the thread.
     */
    @NonNull
    @Override
    Element collect(ReportField reportField, ReportBuilder reportBuilder) {
        Thread t = reportBuilder.getUncaughtExceptionThread();
        final ComplexElement result = new ComplexElement();
        if (t != null) {
            try {
                result.put("id", t.getId());
                result.put("name", t.getName());
                result.put("priority", t.getPriority());
                if (t.getThreadGroup() != null) {
                    result.put("groupName", t.getThreadGroup().getName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return ACRAConstants.NOT_AVAILABLE;
        }
        return result;
    }
}
