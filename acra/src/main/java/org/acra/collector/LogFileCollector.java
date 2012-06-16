package org.acra.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.acra.util.BoundedLinkedList;

/**
 * Collects the N last lines of a text stream. Use this collector if your
 * application handles its own logging system.
 * 
 * @author Kevin Gaudin
 * 
 */
class LogFileCollector {

    /**
     * Private constructor to prevent instantiation.
     */
    private LogFileCollector() {
    };

    public static String collectLogFile(InputStream logStream, int numberOfLines) throws IOException {
        BoundedLinkedList<String> resultBuffer = new BoundedLinkedList<String>(numberOfLines);
        BufferedReader reader = new BufferedReader(new InputStreamReader(logStream), 1024);
        String line = reader.readLine();
        while (line != null) {
            resultBuffer.add(line);
            line = reader.readLine();
        }
        return resultBuffer.toString();
    }
}
