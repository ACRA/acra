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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import android.util.SparseArray;

public class MediaCodecListCollector {
    private static final String COLOR_FORMAT_PREFIX = "COLOR_";
    private static Class<?> mediaCodecListClass = null;
    private static Method getCodecInfoAtMethod = null;
    private static Class<?> mediaCodecInfoClass = null;
    private static Method getNameMethod = null;
    private static Method isEncoderMethod = null;
    private static Method getSupportedTypesMethod = null;
    private static Method getCapabilitiesForTypeMethod = null;
    private static Class<?> codecCapabilitiesClass = null;
    private static Field colorFormatsField = null;
    private static Field profileLevelsField = null;
    private static Field profile = null;
    private static Field level = null;
    private static SparseArray<String> mColorFormatValues = new SparseArray<String>();
    private static SparseArray<String> mColorProfileLevelValues = new SparseArray<String>();

    // static init
    static {
        try {
            mediaCodecListClass = Class.forName("android.media.MediaCodecList");
            // Get methods to retrieve media codec info
            getCodecInfoAtMethod = mediaCodecListClass.getMethod("getCodecInfoAt", int.class);
            mediaCodecInfoClass = Class.forName("android.media.MediaCodecInfo");
            getNameMethod = mediaCodecInfoClass.getMethod("getName");
            isEncoderMethod = mediaCodecInfoClass.getMethod("isEncoder");
            getSupportedTypesMethod = mediaCodecInfoClass.getMethod("getSupportedTypes");
            getCapabilitiesForTypeMethod = mediaCodecInfoClass.getMethod("getCapabilitiesForType", String.class);
            codecCapabilitiesClass = Class.forName("android.media.MediaCodecInfo$CodecCapabilities");
            colorFormatsField = codecCapabilitiesClass.getField("colorFormats");
            profileLevelsField = codecCapabilitiesClass.getField("profileLevels");

            for (Field f : codecCapabilitiesClass.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                        && f.getName().startsWith(COLOR_FORMAT_PREFIX)) {
                    mColorFormatValues.put(f.getInt(null), f.getName());
                }
            }

            Class<?> codecProfileLevelClass = Class.forName("android.media.MediaCodecInfo$CodecProfileLevel");
            for (Field f : codecCapabilitiesClass.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                    mColorProfileLevelValues.put(f.getInt(null), f.getName());
                }
            }

            profile = codecProfileLevelClass.getField("profile");
            level = codecProfileLevelClass.getField("level");

        } catch (ClassNotFoundException e) {
            // NOOP
        } catch (NoSuchMethodException e) {
            // NOOP
        } catch (IllegalArgumentException e) {
            // NOOP
        } catch (IllegalAccessException e) {
            // NOOP
        } catch (SecurityException e) {
            // NOOP
        } catch (NoSuchFieldException e) {
            // NOOP
        }

    }

    public static String collecMediaCodecList() {
        StringBuilder result = new StringBuilder();
        if (mediaCodecListClass != null && mediaCodecInfoClass != null) {
            try {
                // Retrieve list of available media codecs
                int codecCount = (Integer) (mediaCodecListClass.getMethod("getCodecCount").invoke(null));

                // Go through each available media codec
                Object codecInfo = null;
                for (int codecIdx = 0; codecIdx < codecCount; codecIdx++) {
                    result.append("\n");
                    codecInfo = getCodecInfoAtMethod.invoke(null, codecIdx);
                    result.append(codecIdx).append(": ").append(getNameMethod.invoke(codecInfo)).append("\n");
                    result.append("isEncoder: ").append(isEncoderMethod.invoke(codecInfo)).append("\n");
                    String[] supportedTypes = (String[]) getSupportedTypesMethod.invoke(codecInfo);
                    result.append("Supported types: ").append(Arrays.toString(supportedTypes)).append("\n");
                    for (String type : supportedTypes) {
                        result.append(collectCapabilitiesForType(codecInfo, type));
                    }
                    result.append("\n");
                }
            } catch (NoSuchMethodException e) {
                // NOOP
            } catch (IllegalAccessException e) {
                // NOOP
            } catch (InvocationTargetException e) {
                // NOOP
            }
        }
        return result.toString();
    }

    /**
     * Retrieve capabilities (ColorFormats and CodecProfileLevels) for a
     * specific codec type.
     * 
     * @param codecInfo
     * @param type
     * @return A string describing the color formats and codec profile levels
     *         available for a specific codec type.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static String collectCapabilitiesForType(Object codecInfo, String type) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        StringBuilder result = new StringBuilder();
        result.append(type).append(" color formats:");
        Object codecCapabilities = getCapabilitiesForTypeMethod.invoke(codecInfo, type);

        // Color Formats
        int[] colorFormats = (int[]) colorFormatsField.get(codecCapabilities);
        for (int i = 0; i < colorFormats.length; i++) {
            result.append(mColorFormatValues.get(colorFormats[i]));
            if (i < colorFormats.length - 1) {
                result.append(',');
            }
        }
        result.append("\n");

        // Color Profile Levels
        result.append(type).append(" profile levels:");
        Object[] codecProfileLevels = (Object[]) profileLevelsField.get(codecCapabilities);
        for (int i = 0; i < codecProfileLevels.length; i++) {
            result.append(profile.getInt(codecProfileLevels[i])).append('-')
                    .append(level.getInt(codecProfileLevels[i]));
            if (i < codecProfileLevels.length - 1) {
                result.append(',');
            }
        }
        result.append("\n");

        return result.append("\n").toString();
    }
}
