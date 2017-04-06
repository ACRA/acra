package org.acra.file;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Comparator;

/**
 * Orders files from oldest to newest based on their last modified date.
 */
final class LastModifiedComparator implements Comparator<File> {
    @Override
    public int compare(@NonNull File lhs, @NonNull File rhs) {
        final long l = lhs.lastModified();
        final long r = rhs.lastModified();
        return l < r ? -1 : (l == r ? 0 : 1);
    }
}
