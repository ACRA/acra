package org.acra.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamReaderTest {

    private var source = listOf(
        "Line 1",
        "Line 2",
        "Line 3",
        "Line 4",
        "Line 5"
    ).joinToString("\n")

    @Test
    fun testLimit() {
        val streamReader = StreamReader(source.byteInputStream(), limit = 3)
        val text = streamReader.read()
        assertFalse(text.contains('2'))
        assertTrue(text.contains('5'))
    }

}