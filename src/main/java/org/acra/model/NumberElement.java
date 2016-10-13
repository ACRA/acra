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

package org.acra.model;

/**
 * @author F43nd1r
 * @since 13.10.2016
 */

public class NumberElement implements Element {
    private final Number content;

    public NumberElement(Number content) {
        this.content = content;
    }

    @Override
    public Object value() {
        return content;
    }

    @Override
    public String[] flatten() {
        return new String[]{toString()};
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
