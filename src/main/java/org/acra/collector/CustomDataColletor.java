package org.acra.collector;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lukas on 12.08.2016.
 */
public class CustomDataColletor extends Collector {
    private final Map<String, String> customParameters;

    public CustomDataColletor(Map<String,String> customParameters){
        super(ReportField.CUSTOM_DATA);
        this.customParameters = customParameters;
    }
    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
        return createCustomInfoString(reportBuilder.getCustomData());
    }


    /**
     * Generates the string which is posted in the single custom data field in
     * the GoogleDocs Form.
     *
     * @return A string with a 'key = value' pair on each line.
     */
    @NonNull
    private String createCustomInfoString(@Nullable Map<String, String> reportCustomData) {
        Map<String, String> params = customParameters;

        if (reportCustomData != null) {
            params = new HashMap<String, String>(params);
            params.putAll(reportCustomData);
        }

        final StringBuilder customInfo = new StringBuilder();
        for (final Map.Entry<String, String> currentEntry : params.entrySet()) {
            customInfo.append(currentEntry.getKey());
            customInfo.append(" = ");

            // We need to escape new lines in values or they are transformed into new
            // custom fields. => let's replace all '\n' with "\\n"
            final String currentVal = currentEntry.getValue();
            if (currentVal != null) {
                customInfo.append(currentVal.replaceAll("\n", "\\\\n"));
            } else {
                customInfo.append("null");
            }
            customInfo.append('\n');
        }
        return customInfo.toString();
    }
}
