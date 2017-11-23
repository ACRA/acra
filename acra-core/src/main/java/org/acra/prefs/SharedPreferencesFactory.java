package org.acra.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;

/**
 * Responsible for creating a SharedPreferences instance which stores ACRA settings.
 * <p>
 * Retrieves the {@link SharedPreferences} instance where user adjustable settings for ACRA are stored.
 * Default are the Application default SharedPreferences, but you can provide another SharedPreferences name with {@link org.acra.annotation.AcraCore#sharedPreferencesName()}.
 * </p>
 */
public class SharedPreferencesFactory {

    private final Context context;
    private final CoreConfiguration config;

    public SharedPreferencesFactory(@NonNull Context context, @NonNull CoreConfiguration config) {
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
        } else if (!ACRAConstants.DEFAULT_STRING_VALUE.equals(config.sharedPreferencesName())) {
            return context.getSharedPreferences(config.sharedPreferencesName(), Context.MODE_PRIVATE);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
