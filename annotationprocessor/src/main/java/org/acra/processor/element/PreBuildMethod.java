/*
 * Copyright (c) 2018 the ACRA team
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

package org.acra.processor.element;

import android.support.annotation.NonNull;

import org.acra.processor.creator.BuildMethodCreator;

import java.util.Collections;

/**
 * @author F43nd1r
 * @since 11.01.2018
 */

class PreBuildMethod extends AbstractElement implements ValidatedElement {
    PreBuildMethod(@NonNull String name) {
        super(name, null, Collections.emptyList());
    }

    @Override
    public void addToBuildMethod(@NonNull BuildMethodCreator method) {
        method.addDelegateCall(getName());
    }
}
