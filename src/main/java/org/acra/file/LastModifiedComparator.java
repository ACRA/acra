package org.acra.file;

import java.io.File;
import java.util.Comparator;

/**
 * Orders files from oldest to newest based on their last modified date.
 */
public final class LastModifiedComparator implements Comparator<File> {
    @Override
    public int compare(File lhs, File rhs) {
        return (int) (lhs.lastModified() - rhs.lastModified());
    }
}
