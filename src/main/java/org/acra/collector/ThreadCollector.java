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

/**
 * Collects some data identifying a Thread, usually the Thread which crashed.
 * 
 * @author Kevin Gaudin
 * 
 */
public class ThreadCollector {

    /**
     * Convenience method that collects some data identifying a Thread, usually the Thread which
     * crashed and returns a string containing the thread's id, name, priority and group name.
     * 
     * @param t the thread
     * @return a string representation of the string including the id, name and priority of the thread.
     */
    public static String collect(Thread t) {
        StringBuilder result = new StringBuilder();
        if (t != null) {

            result.append("id=").append(t.getId()).append("\n");
            result.append("name=").append(t.getName()).append("\n");
            result.append("priority=").append(t.getPriority()).append("\n");
            if (t.getThreadGroup() != null) {
                result.append("groupName=").append(t.getThreadGroup().getName()).append("\n");
            }
        } else {
            result.append("No broken thread, this might be a silent exception.");
        }
        return result.toString();
    }
}
