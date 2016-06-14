/*
 *  Copyright 2016
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
package org.acra.log;

import org.acra.ACRA;

/**
 * Logs debug and verbose messages only if {@link ACRA#DEV_LOGGING} is true
 *
 * @author F43nd1r
 * @since 4.9.1
 */
public class DebugConditionalAndroidLog extends AndroidLogDelegate {
    @Override
    public int v(String tag, String msg) {
        if (ACRA.DEV_LOGGING) {
            return super.v(tag, msg);
        }
        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        if (ACRA.DEV_LOGGING) {
            return super.v(tag, msg, tr);
        }
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        if (ACRA.DEV_LOGGING) {
            return super.d(tag, msg);
        }
        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        if (ACRA.DEV_LOGGING) {
            return super.d(tag, msg, tr);
        }
        return 0;
    }
}
