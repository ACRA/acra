/*
 * Copyright (c) 2017 the ACRA team
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

package org.acra.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author F43nd1r
 * @since 29.11.2017
 */
@RunWith(RobolectricTestRunner.class)
public class InstallationTest {
    @Test
    public void id() {
        final String id = Installation.id(RuntimeEnvironment.application);
        assertEquals(id, Installation.id(RuntimeEnvironment.application));
        for(File child : RuntimeEnvironment.application.getFilesDir().listFiles()){
            if(child.isFile()) child.delete();
        }
        assertThat(Installation.id(RuntimeEnvironment.application), not(id));
    }

}