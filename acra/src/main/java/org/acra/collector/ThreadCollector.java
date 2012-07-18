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

public class ThreadCollector {
    public static String collect(Thread t) {
        StringBuilder result = new StringBuilder();

        result.append("id=").append(t.getId());
        result.append("name=").append(t.getName());
        result.append("priority=").append(t.getPriority());
        if (t.getThreadGroup() != null) {
            result.append("groupName=").append(t.getThreadGroup().getName());
        }

        return result.toString();
    }
}
