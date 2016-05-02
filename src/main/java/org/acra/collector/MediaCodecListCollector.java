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

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Collects data about available codecs on the device through the MediaCodecList
 * API introduced in Android 4.1 JellyBean.
 *
 * @author Kevin Gaudin
 */
final class MediaCodecListCollector {
    private MediaCodecListCollector(){}
    private enum CodecType {
        AVC, H263, MPEG4, AAC

    }

    private static final String COLOR_FORMAT_PREFIX = "COLOR_";
    private static final String[] MPEG4_TYPES = { "mp4", "mpeg4", "MP4", "MPEG4" };
    private static final String[] AVC_TYPES = { "avc", "h264", "AVC", "H264" };
    private static final String[] H263_TYPES = { "h263", "H263" };
    private static final String[] AAC_TYPES = { "aac", "AAC" };

    private static final SparseArray<String> mColorFormatValues = new SparseArray<String>();
    private static final SparseArray<String> mAVCLevelValues = new SparseArray<String>();
    private static final SparseArray<String> mAVCProfileValues = new SparseArray<String>();
    private static final SparseArray<String> mH263LevelValues = new SparseArray<String>();
    private static final SparseArray<String> mH263ProfileValues = new SparseArray<String>();
    private static final SparseArray<String> mMPEG4LevelValues = new SparseArray<String>();
    private static final SparseArray<String> mMPEG4ProfileValues = new SparseArray<String>();
    private static final SparseArray<String> mAACProfileValues = new SparseArray<String>();

    // static init where nearly all reflection inspection is done.
    static {
        try {
            final Class<?> codecCapabilitiesClass = Class.forName("android.media.MediaCodecInfo$CodecCapabilities");

            // Retrieve list of possible Color Format
            for (Field f : codecCapabilitiesClass.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                        && f.getName().startsWith(COLOR_FORMAT_PREFIX)) {
                    mColorFormatValues.put(f.getInt(null), f.getName());
                }
            }

            // Retrieve lists of possible codecs profiles and levels
            final Class<?> codecProfileLevelClass = Class.forName("android.media.MediaCodecInfo$CodecProfileLevel");
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
        } catch (@NonNull ClassNotFoundException ignored) {
            // NOOP
        } catch (@NonNull SecurityException ignored) {
            // NOOP
        } catch (@NonNull IllegalAccessException ignored) {
            // NOOP
        } catch (@NonNull IllegalArgumentException ignored) {
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
    @NonNull
    public static String collectMediaCodecList() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return "";
        }

        final MediaCodecInfo[] infos;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            final int codecCount = MediaCodecList.getCodecCount();
            infos = new MediaCodecInfo[codecCount];
            for (int codecIdx = 0; codecIdx < codecCount; codecIdx++) {
                //noinspection deprecation
                infos[codecIdx] = MediaCodecList.getCodecInfoAt(codecIdx);
            }
        } else {
            infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        }

        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < infos.length; i++) {
            final MediaCodecInfo codecInfo = infos[i];
            result.append('\n')
                    .append(i).append(": ").append(codecInfo.getName()).append('\n')
                    .append("isEncoder: ").append(codecInfo.isEncoder()).append('\n');

            final String[] supportedTypes = codecInfo.getSupportedTypes();
            result.append("Supported types: ").append(Arrays.toString(supportedTypes)).append('\n');
            for (String type : supportedTypes) {
                result.append(collectCapabilitiesForType(codecInfo, type));
            }
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * Retrieve capabilities (ColorFormats and CodecProfileLevels) for a
     * specific codec type.
     *
     * @param codecInfo //TODO describe param
     * @param type      //TODO describe param
     * @return A string describing the color formats and codec profile levels
     * available for a specific codec type.
     */
    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static String collectCapabilitiesForType(@NonNull final MediaCodecInfo codecInfo, @NonNull String type){

        final StringBuilder result = new StringBuilder();
        final MediaCodecInfo.CodecCapabilities codecCapabilities = codecInfo.getCapabilitiesForType(type);

        // Color Formats
        final int[] colorFormats = codecCapabilities.colorFormats;
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

        final CodecType codecType = identifyCodecType(codecInfo);

        // Profile Levels
        final MediaCodecInfo.CodecProfileLevel[] codecProfileLevels = codecCapabilities.profileLevels;
        if (codecProfileLevels.length > 0) {
            result.append(type).append(" profile levels:");
            for (int i = 0; i < codecProfileLevels.length; i++) {

                final int profileValue = codecProfileLevels[i].profile;
                final int levelValue = codecProfileLevels[i].level;

                if (codecType == null) {
                    // Unknown codec
                    result.append(profileValue).append('-').append(levelValue);
                    break;
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
            result.append('\n');
        }
        return result.append('\n').toString();
    }

    /**
     * Looks for keywords in the codec name to identify its nature ({@link CodecType}).
     *
     * @param codecInfo //TODO describe param
     * @return //TODO describe return
     */
    @Nullable
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static CodecType identifyCodecType(@NonNull MediaCodecInfo codecInfo)  {

        final String name = codecInfo.getName();
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
