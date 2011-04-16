package org.acra;

import java.lang.reflect.Field;

public class FieldsCollector {
    public static String getConstants(Class<? extends Object> someClass) {
        StringBuilder result = new StringBuilder();

        Field[] fields = someClass.getFields();
        for (Field field : fields) {
            result.append(field.getName()).append("=");
            try {
                result.append(field.get(null).toString());
            } catch (IllegalArgumentException e) {
                result.append("N/A");
            } catch (IllegalAccessException e) {
                result.append("N/A");
            }
            result.append("\n");
        }

        return result.toString();
    }
}
