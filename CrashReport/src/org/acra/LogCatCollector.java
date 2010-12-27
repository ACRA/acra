package org.acra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

class LogCatCollector {

        protected static StringBuilder collectLogCat(String bufferName){
            final StringBuilder log = new StringBuilder();
            try{
                ArrayList<String> commandLine = new ArrayList<String>();
                commandLine.add("logcat");
                if(bufferName != null) {
                    commandLine.add("-b");
                    commandLine.add(bufferName);
                }
                commandLine.addAll(Arrays.asList(ACRA.getConfig().logcatArguments()));
                
                Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                while ((line = bufferedReader.readLine()) != null){ 
                    log.append(line);
                    log.append("\n"); 
                }
            } 
            catch (IOException e){
                Log.e(ACRA.LOG_TAG, "CollectLogTask.doInBackground failed", e);
            } 

            return log;
        }
    }