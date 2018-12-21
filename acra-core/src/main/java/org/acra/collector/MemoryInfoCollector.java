/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.collector;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.util.StreamReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

/**
 * Collects results of the <code>dumpsys</code> command.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector.class)
public final class MemoryInfoCollector extends BaseReportFieldCollector {
    public MemoryInfoCollector() {
        super(ReportField.DUMPSYS_MEMINFO, ReportField.TOTAL_MEM_SIZE, ReportField.AVAILABLE_MEM_SIZE);
    }

    @Override
    boolean shouldCollect(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportField collect, @NonNull ReportBuilder reportBuilder) {
        return super.shouldCollect(context, config, collect, reportBuilder) && !(reportBuilder.getException() instanceof OutOfMemoryError);
    }

    @Override
    void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) {
        switch (reportField) {
            case DUMPSYS_MEMINFO:
                target.put(ReportField.DUMPSYS_MEMINFO, collectMemInfo());
                break;
            case TOTAL_MEM_SIZE:
                target.put(ReportField.TOTAL_MEM_SIZE, getTotalInternalMemorySize());
                break;
            case AVAILABLE_MEM_SIZE:
                target.put(ReportField.AVAILABLE_MEM_SIZE, getAvailableInternalMemorySize());
                break;
            default:
                //will not happen if used correctly
                throw new IllegalArgumentException();
        }
    }

    /**
     * Collect results of the <code>dumpsys meminfo</code> command restricted to this application process.
     *
     * @return The execution result.
     */
    @Nullable
    private String collectMemInfo() {
        try {
            final List<String> commandLine = new ArrayList<>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            return new StreamReader(process.getInputStream()).read();
        } catch (IOException e) {
            ACRA.log.e(LOG_TAG, "MemoryInfoCollector.meminfo could not retrieve data", e);
            return null;
        }
    }

    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    private long getAvailableInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize;
        final long availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            //noinspection deprecation
            blockSize = stat.getBlockSize();
            //noinspection deprecation
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * Calculates the total memory of the device. This is based on an inspection of the filesystem, which in android devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    private long getTotalInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize;
        final long totalBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        } else {
            //noinspection deprecation
            blockSize = stat.getBlockSize();
            //noinspection deprecation
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

}