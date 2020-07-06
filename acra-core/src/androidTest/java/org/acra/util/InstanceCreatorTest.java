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

package org.acra.util;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class InstanceCreatorTest {
    private InstanceCreator instanceCreator;

    @Before
    public void setUp() {
        instanceCreator = new InstanceCreator();
    }

    @Test
    public void create() {
        assertNotNull(instanceCreator.create(ClassWithDefaultConstructor.class));
        assertNotNull(instanceCreator.create(ClassWithExplicitNoArgsConstructor.class));
        assertNull(instanceCreator.create(ClassWithPrivateConstructor.class));
        assertNull(instanceCreator.create(ClassWithImplicitConstructorArg.class));
        assertNull(instanceCreator.create(ClassWithExplicitConstructorArg.class));
    }

    @Test
    public void create1() {
        assertThat(instanceCreator.create(Arrays.asList(ClassWithDefaultConstructor.class, ClassWithExplicitConstructorArg.class)), hasSize(1));
    }

    public static class ClassWithDefaultConstructor {
    }

    public static class ClassWithExplicitNoArgsConstructor {
        public ClassWithExplicitNoArgsConstructor() {
            //nothing
        }
    }

    public static class ClassWithPrivateConstructor {
        private ClassWithPrivateConstructor(){
            //nothing
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class ClassWithImplicitConstructorArg {
    }

    public static class ClassWithExplicitConstructorArg {
        public ClassWithExplicitConstructorArg(@SuppressWarnings("unused") String arg){
            //nothing
        }
    }
}