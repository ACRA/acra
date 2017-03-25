package org.acra.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;

/**
 * Responsible for creating a SharedPreferences instance which stores ACRA settings.
 * <p>
 * Retrieves the {@link SharedPreferences} instance where user adjustable
 * settings for ACRA are stored. Default are the Application default
 * SharedPreferences, but you can provide another SharedPreferences name
 * with {@link ReportsCrashes#sharedPreferencesName()}.
 * </p>
 */
public class SharedPreferencesFactory {

    private final Context context;
    private final ACRAConfiguration config;

    public SharedPreferencesFactory(@NonNull Context context, @NonNull ACRAConfiguration config) {
        this.context = context;
        this.config = config;
    }

    /**
     * @return The Shared Preferences where ACRA will retrieve its user adjustable setting.
     */
    @NonNull
    public SharedPreferences create() {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalStateException("Cannot call ACRA.getACRASharedPreferences() before ACRA.init().");
        } else if (!"".equals(config.sharedPreferencesName())) {
            return context.getSharedPreferences(config.sharedPreferencesName(), config.sharedPreferencesMode());
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
