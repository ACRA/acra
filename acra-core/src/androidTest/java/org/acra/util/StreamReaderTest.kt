package org.acra.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

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

    @Test(timeout = 5_000)
    fun testTimeoutReturnsContent() {
        val text = StreamReader(source.byteInputStream()).setTimeout(100).read()

        assertEquals(source, text)
    }

    @Test(timeout = 5_000)
    fun testTimeoutReturnsOnceTimeoutIsReached() {
        // A stream with nothing buffered and no EOF: reading it must be bounded by the timeout.
        val idleStream = object : InputStream() {
            override fun read(): Int {
                Thread.sleep(Long.MAX_VALUE)
                return -1
            }

            override fun available(): Int = 0
        }

        val start = System.nanoTime()
        val text = StreamReader(idleStream).setTimeout(200).read()
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000

        assertEquals("", text)
        assertTrue(
            "read() returned after $elapsedMillis ms, expected the 200 ms timeout to bound it",
            elapsedMillis < 2_000
        )
    }

}