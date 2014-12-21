/*
 *  Copyright 2012 Kevin Gaudin
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

/**
 * Collects data about available codecs on the device through the MediaCodecList
 * API introduced in Android 4.1 JellyBean.
 * 
 * @author Kevin Gaudin
 * 
 */
public class MediaCodecListCollector {
    private enum CodecType {
        AVC, H263, MPEG4, AAC

    }

    private static final String COLOR_FORMAT_PREFIX = "COLOR_";
    private static final String[] MPEG4_TYPES = { "mp4", "mpeg4", "MP4", "MPEG4" };
    private static final String[] AVC_TYPES = { "avc", "h264", "AVC", "H264" };
    private static final String[] H263_TYPES = { "h263", "H263" };
    private static final String[] AAC_TYPES = { "aac", "AAC" };

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
    private static Field profileField = null;
    private static Field levelField = null;
    private static SparseArray<String> mColorFormatValues = new SparseArray<String>();
    private static SparseArray<String> mAVCLevelValues = new SparseArray<String>();
    private static SparseArray<String> mAVCProfileValues = new SparseArray<String>();
    private static SparseArray<String> mH263LevelValues = new SparseArray<String>();
    private static SparseArray<String> mH263ProfileValues = new SparseArray<String>();
    private static SparseArray<String> mMPEG4LevelValues = new SparseArray<String>();
    private static SparseArray<String> mMPEG4ProfileValues = new SparseArray<String>();
    private static SparseArray<String> mAACProfileValues = new SparseArray<String>();

    // static init where nearly all reflection inspection is done.
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

            // Retrieve list of possible Color Format
            for (Field f : codecCapabilitiesClass.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                        && f.getName().startsWith(COLOR_FORMAT_PREFIX)) {
                    mColorFormatValues.put(f.getInt(null), f.getName());
                }
            }

            // Retrieve lists of possible codecs profiles and levels
            Class<?> codecProfileLevelClass = Class.forName("android.media.MediaCodecInfo$CodecProfileLevel");
            for (Field f : codecProfileLevelClass.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                    if (f.getName().startsWith("AVCLevel")) {
                        mAVCLevelValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("AVCProfile")) {
                        mAVCProfileValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("H263Level")) {
                        mH263LevelValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("H263Profile")) {
                        mH263ProfileValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("MPEG4Level")) {
                        mMPEG4LevelValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("MPEG4Profile")) {
                        mMPEG4ProfileValues.put(f.getInt(null), f.getName());
                    } else if (f.getName().startsWith("AAC")) {
                        mAACProfileValues.put(f.getInt(null), f.getName());
                    }
                }
            }

            profileField = codecProfileLevelClass.getField("profile");
            levelField = codecProfileLevelClass.getField("level");

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

    /**
     * Builds a String describing the list of available codecs on the device
     * with their capabilities (supported Color Formats, Codec Profiles et
     * Levels).
     * 
     * @return The media codecs information
     */
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

        Object codecCapabilities = getCapabilitiesForTypeMethod.invoke(codecInfo, type);

        // Color Formats
        int[] colorFormats = (int[]) colorFormatsField.get(codecCapabilities);
        if (colorFormats.length > 0) {
            result.append(type).append(" color formats:");
            for (int i = 0; i < colorFormats.length; i++) {
                result.append(mColorFormatValues.get(colorFormats[i]));
                if (i < colorFormats.length - 1) {
                    result.append(',');
                }
            }
            result.append("\n");
        }

        // Profile Levels
        Object[] codecProfileLevels = (Object[]) profileLevelsField.get(codecCapabilities);
        if (codecProfileLevels.length > 0) {
            result.append(type).append(" profile levels:");
            for (int i = 0; i < codecProfileLevels.length; i++) {

                CodecType codecType = identifyCodecType(codecInfo);
                int profileValue = profileField.getInt(codecProfileLevels[i]);
                int levelValue = levelField.getInt(codecProfileLevels[i]);

                if (codecType == null) {
                    // Unknown codec
                    result.append(profileValue).append('-').append(levelValue);
                }

                switch (codecType) {
                case AVC:
                    result.append(profileValue).append(mAVCProfileValues.get(profileValue)).append('-')
                            .append(mAVCLevelValues.get(levelValue));
                    break;
                case H263:
                    result.append(mH263ProfileValues.get(profileValue)).append('-')
                            .append(mH263LevelValues.get(levelValue));
                    break;
                case MPEG4:
                    result.append(mMPEG4ProfileValues.get(profileValue)).append('-')
                            .append(mMPEG4LevelValues.get(levelValue));
                    break;
                case AAC:
                    result.append(mAACProfileValues.get(profileValue));
                    break;
                default:
                    break;
                }

                if (i < codecProfileLevels.length - 1) {
                    result.append(',');
                }

            }
            result.append("\n");
        }
        return result.append("\n").toString();
    }

    /**
     * Looks for keywords in the codec name to identify its nature ({@link CodecType}).
     * @param codecInfo
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static CodecType identifyCodecType(Object codecInfo) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        String name = (String) getNameMethod.invoke(codecInfo);
        for (String token : AVC_TYPES) {
            if (name.contains(token)) {
                return CodecType.AVC;
            }
        }
        for (String token : H263_TYPES) {
            if (name.contains(token)) {
                return CodecType.H263;
            }
        }
        for (String token : MPEG4_TYPES) {
            if (name.contains(token)) {
                return CodecType.MPEG4;
            }
        }
        for (String token : AAC_TYPES) {
            if (name.contains(token)) {
                return CodecType.AAC;
            }
        }

        return null;
    }
}
