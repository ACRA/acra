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

package org.acra.config;

import android.app.Application;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.acra.annotation.AcraCore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author F43nd1r
 * @since 01.02.18
 */
@RunWith(AndroidJUnit4.class)
public class CoreConfigurationBuilderTest {

    @Test
    public void enabled() {
        assertTrue(new CoreConfigurationBuilder(new AnnotatedClass()).getEnabled());
        assertFalse(new CoreConfigurationBuilder(new NonAnnotatedClass()).getEnabled());
    }

    @AcraCore
    private static class AnnotatedClass extends Application {

    }

    private static class NonAnnotatedClass extends Application {

    }
}