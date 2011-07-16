/*
 * Class copied from the Android Developers Blog:
 * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html 
 */
package org.acra.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import org.acra.ACRA;

import android.content.Context;
import android.util.Log;

/**
 * <p>
 * Creates a file storing a UUID on the first application start. This UUID can then be used as a identifier of this
 * specific application installation.
 * </p>
 * 
 * <p>
 * This was taken from <a href="http://android-developers.blogspot.com/2011/03/identifying-app-installations.html"> the
 * android developers blog.</a>
 * </p>
 */
public class Installation {

    private static String sID;
    private static final String INSTALLATION = "ACRA-INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            final File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (IOException e) {
                Log.w(ACRA.LOG_TAG, "Couldn't retrieve InstallationId for " + context.getPackageName(), e);
                return "Couldn't retrieve InstallationId";
            } catch (RuntimeException e) {
                Log.w(ACRA.LOG_TAG, "Couldn't retrieve InstallationId for " + context.getPackageName(), e);
                return "Couldn't retrieve InstallationId";
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        final RandomAccessFile f = new RandomAccessFile(installation, "r");
        final byte[] bytes = new byte[(int) f.length()];
        try {
            f.readFully(bytes);
        } finally {
            f.close();
        }
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        final FileOutputStream out = new FileOutputStream(installation);
        try {
            final String id = UUID.randomUUID().toString();
            out.write(id.getBytes());
        } finally {
            out.close();
        }
    }
}
