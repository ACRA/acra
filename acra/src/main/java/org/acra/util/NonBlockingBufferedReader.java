package org.acra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronously reads a buffer into a List of String.
 *
 * @author C-Romeo
 * @since 4.9.0
 */
final class NonBlockingBufferedReader {

    private final BlockingQueue<String> lines = new LinkedBlockingQueue<String>();
    private Thread backgroundReaderThread = null;
    private volatile IOException exception = null;

    NonBlockingBufferedReader(final BufferedReader bufferedReader) {
        backgroundReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        final String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        lines.add(line);
                    }
                } catch (IOException e) {
                    exception = e;
                } finally {
                    IOUtils.safeClose(bufferedReader);
                }
            }
        });
        backgroundReaderThread.setDaemon(true);
        backgroundReaderThread.start();
    }

    String readLine() throws InterruptedException, IOException {
        if(exception != null){
            throw exception;
        }
        return lines.isEmpty() ? null : lines.poll(500L, TimeUnit.MILLISECONDS);
    }

    void close() {
        if (backgroundReaderThread != null) {
            backgroundReaderThread.interrupt();
            backgroundReaderThread = null;
        }
    }
}
