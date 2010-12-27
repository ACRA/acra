package org.acra;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

public class DropBoxCollector {
    private static final String[] SYSTEM_TAGS = { "system_server_anr", "system_server_wtf", "system_server_crash",
        "system_app_anr", "system_app_wtf", "system_app_crash", "system_server_anr",
        "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO",
        "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE",
        "APANIC_THREADS", "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode" };

    public static String read(Context context, String[] additionalTags) {
        try {
            String serviceName = getDropBoxServiceName();
            if (serviceName != null) {
                StringBuilder dropboxContent = new StringBuilder();
                Object dropbox = context.getSystemService(serviceName);
                Method getNextEntry = dropbox.getClass().getMethod("getNextEntry", String.class, long.class);
                if (getNextEntry != null) {
                    Time timer = new Time();
                    timer.setToNow();
                    timer.minute -= ACRA.getConfig().dropboxCollectionMinutes();
                    timer.normalize(false);
                    long time = timer.toMillis(false);
                    ArrayList<String> tags = new ArrayList<String>(Arrays.asList(SYSTEM_TAGS)); 
                    if(additionalTags != null && additionalTags.length > 0) {
                        tags.addAll(Arrays.asList(additionalTags));
                    }
                    String text = null;
                    Object entry = null;
                    for (String tag : tags) {
                        long msec = time;
                        dropboxContent.append("Tag: ").append(tag).append('\n');
                        entry = getNextEntry.invoke(dropbox, tag, msec);
                        if (entry != null) {
                            Method getText = entry.getClass().getMethod("getText", int.class);
                            Method getTimeMillis = entry.getClass().getMethod("getTimeMillis", (Class[]) null);
                            Method close = entry.getClass().getMethod("close", (Class[]) null);
                            while (entry != null) {
                                msec = (Long) getTimeMillis.invoke(entry, (Object[]) null);
                                timer.set(msec);
                                dropboxContent.append("@").append(timer.format2445()).append('\n');
                                text = (String) getText.invoke(entry, 500);
                                if (text != null) {
                                    dropboxContent.append("Text: ").append(text).append('\n');
                                } else {
                                    dropboxContent.append("Not Text!").append('\n');
                                }
                                close.invoke(entry, (Object[]) null);
                                entry = getNextEntry.invoke(dropbox, tag, msec);
                            }
                        } else {
                            dropboxContent.append("Nothing.").append('\n');
                        }
                        
                    }
                }
                return dropboxContent.toString();
            }
        } catch (SecurityException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (NoSuchMethodException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (IllegalArgumentException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (IllegalAccessException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (InvocationTargetException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        } catch (NoSuchFieldException e) {
            Log.e(ACRA.LOG_TAG, "Error : ", e);
        }
        return "N/A";
    }

    /**
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * 
     */
    private static String getDropBoxServiceName() throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field serviceName = Context.class.getField("DROPBOX_SERVICE");
        if (serviceName != null) {
            return (String) serviceName.get(null);
        }
        return null;
    }
}
