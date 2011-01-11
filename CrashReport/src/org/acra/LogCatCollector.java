package org.acra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import util.BoundedLinkedList;

import android.util.Log;

class LogCatCollector {

    private static final int DEFAULT_TAIL_COUNT = 100;

    protected static String collectLogCat(String bufferName) {
        BoundedLinkedList<String> logcatBuf = null;
        try {
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            if (bufferName != null) {
                commandLine.add("-b");
                commandLine.add(bufferName);
            }
            // "-t n" argument has been introduced in FroYo (API level 8). For
            // devices with lower API level, we will have to emulate its job.
            boolean fakeTail = false;
            int tailCount = -1;
            List<String> logcatArgumentsList = new ArrayList<String>(Arrays.asList(ACRA.getConfig().logcatArguments()));

            int tailIndex = logcatArgumentsList.indexOf("-t");
            if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
                tailCount = Integer.parseInt(logcatArgumentsList.remove(tailIndex + 1));
                if (Compatibility.getAPILevel() < 8) {

                    logcatArgumentsList.remove(tailIndex);
                    logcatArgumentsList.add("-d");
                    fakeTail = true;
                }
            }
            logcatBuf = new BoundedLinkedList<String>(tailCount > 0 ? tailCount : DEFAULT_TAIL_COUNT);
            commandLine.addAll(logcatArgumentsList);

            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logcatBuf.add(line);
                logcatBuf.add("\n");
            }

        } catch (IOException e) {
            Log.e(ACRA.LOG_TAG, "CollectLogTask.doInBackground failed", e);
        }

        return logcatBuf.toString();
    }

    /**
     * Keeps only a given number of trailing lines from a StringBuilder.
     * 
     * @param sBuilder
     * @param linesToKeep
     */
    private static void tail(StringBuilder sBuilder, int linesToKeep) {
        int idxNewLine = sBuilder.indexOf("\n");
        ArrayList<Integer> listNewLines = new ArrayList<Integer>();

        while (idxNewLine != -1) {
            listNewLines.add(idxNewLine);
        }
        if (listNewLines.size() > linesToKeep) {
            int idxTruncate = listNewLines.get(listNewLines.size() - 1 - linesToKeep);
            sBuilder.delete(0, idxTruncate + 1); // exclude NewLine char
        }
    }
}