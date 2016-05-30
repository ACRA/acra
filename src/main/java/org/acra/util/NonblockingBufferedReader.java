package org.acra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author C-Romeo
 * @since 4.9.0
 */
public class NonblockingBufferedReader {
    private final BlockingQueue<String> lines = new LinkedBlockingQueue<String>();
    private boolean closed = false;
    private Thread backgroundReaderThread = null;

    public NonblockingBufferedReader(final BufferedReader bufferedReader) {
        backgroundReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        lines.add(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    closed = true;
                    IOUtils.safeClose(bufferedReader);
                }
            }
        });
        backgroundReaderThread.setDaemon(true);
        backgroundReaderThread.start();
    }

    public String readLine() throws InterruptedException {
        return closed && lines.isEmpty() ? null : lines.poll(500L, TimeUnit.MILLISECONDS);
    }

    public void close() {
        if (backgroundReaderThread != null) {
            backgroundReaderThread.interrupt();
            backgroundReaderThread = null;
        }
    }
}
