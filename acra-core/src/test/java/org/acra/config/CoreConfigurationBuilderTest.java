package org.acra.config;

import android.app.Application;
import android.os.Build;

import org.acra.annotation.AcraCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author F43nd1r
 * @since 01.02.18
 */
@Config(sdk = Build.VERSION_CODES.P)
@RunWith(RobolectricTestRunner.class)
public class CoreConfigurationBuilderTest {

    @Test
    public void enabled() {
        assertTrue(new CoreConfigurationBuilder(new AnnotatedClass()).enabled());
        assertFalse(new CoreConfigurationBuilder(new NonAnnotatedClass()).enabled());
    }

    @AcraCore
    private static class AnnotatedClass extends Application {

    }

    private static class NonAnnotatedClass extends Application {

    }
}