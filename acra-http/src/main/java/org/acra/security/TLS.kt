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
package org.acra.security

import android.os.Build

enum class TLS(val id: String, val minSdk: Int) {
    V1("TLSv1", Build.VERSION_CODES.BASE),
    V1_1("TLSv1.1", Build.VERSION_CODES.JELLY_BEAN),
    V1_2("TLSv1.2", Build.VERSION_CODES.JELLY_BEAN),
    V1_3("TLSv1.3", Build.VERSION_CODES.Q);
}