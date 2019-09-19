package org.acra.util;

import android.os.Build;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
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

    public class ClassWithImplicitConstructorArg {
    }

    public static class ClassWithExplicitConstructorArg {
        public ClassWithExplicitConstructorArg(@SuppressWarnings("unused") String arg){
            //nothing
        }
    }
}