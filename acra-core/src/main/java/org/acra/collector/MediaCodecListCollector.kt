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
package org.acra.collector

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.util.SparseArray
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Modifier

/**
 * Collects data about available codecs on the device through the MediaCodecList API introduced in Android 4.1 JellyBean.
 *
 * @author Kevin Gaudin &amp; F43nd1r
 */
@AutoService(Collector::class)
class MediaCodecListCollector : BaseReportFieldCollector(ReportField.MEDIA_CODEC_LIST) {
    private enum class CodecType {
        AVC, H263, MPEG4, AAC
    }

    private val mColorFormatValues = SparseArray<String>()
    private val mAVCLevelValues = SparseArray<String>()
    private val mAVCProfileValues = SparseArray<String>()
    private val mH263LevelValues = SparseArray<String>()
    private val mH263ProfileValues = SparseArray<String>()
    private val mMPEG4LevelValues = SparseArray<String>()
    private val mMPEG4ProfileValues = SparseArray<String>()
    private val mAACProfileValues = SparseArray<String>()
    override val order: Collector.Order
        get() = Collector.Order.LATE

    @Throws(JSONException::class)
    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        target.put(ReportField.MEDIA_CODEC_LIST, collectMediaCodecList())
    }

    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return super.shouldCollect(context, config, collect, reportBuilder) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    }

    /**
     * use reflection to prepare field arrays.
     */
    private fun prepare() {
        try {
            val codecCapabilitiesClass = Class.forName("android.media.MediaCodecInfo\$CodecCapabilities")

            // Retrieve list of possible Color Format
            for (f in codecCapabilitiesClass.fields) {
                if (Modifier.isStatic(f.modifiers) && Modifier.isFinal(f.modifiers)
                    && f.name.startsWith(COLOR_FORMAT_PREFIX)
                ) {
                    mColorFormatValues.put(f.getInt(null), f.name)
                }
            }

            // Retrieve lists of possible codecs profiles and levels
            val codecProfileLevelClass = Class.forName("android.media.MediaCodecInfo\$CodecProfileLevel")
            for (f in codecProfileLevelClass.fields) {
                if (Modifier.isStatic(f.modifiers) && Modifier.isFinal(f.modifiers)) {
                    when {
                        f.name.startsWith("AVCLevel") -> mAVCLevelValues.put(f.getInt(null), f.name)
                        f.name.startsWith("AVCProfile") -> mAVCProfileValues.put(f.getInt(null), f.name)
                        f.name.startsWith("H263Level") -> mH263LevelValues.put(f.getInt(null), f.name)
                        f.name.startsWith("H263Profile") -> mH263ProfileValues.put(f.getInt(null), f.name)
                        f.name.startsWith("MPEG4Level") -> mMPEG4LevelValues.put(f.getInt(null), f.name)
                        f.name.startsWith("MPEG4Profile") -> mMPEG4ProfileValues.put(f.getInt(null), f.name)
                        f.name.startsWith("AAC") -> mAACProfileValues.put(f.getInt(null), f.name)
                    }
                }
            }
        } catch (ignored: ClassNotFoundException) {
            // NOOP
        } catch (ignored: SecurityException) {
            // NOOP
        } catch (ignored: IllegalAccessException) {
            // NOOP
        } catch (ignored: IllegalArgumentException) {
            // NOOP
        }
    }

    /**
     * Builds a JSONObject describing the list of available codecs on the device with their capabilities (supported Color Formats, Codec Profiles and Levels).
     *
     * @return The media codecs information
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Throws(JSONException::class)
    private fun collectMediaCodecList(): JSONObject {
        prepare()
        val infos: Array<MediaCodecInfo?> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
        } else {
            @Suppress("DEPRECATION")
            (0 until MediaCodecList.getCodecCount()).map { MediaCodecList.getCodecInfoAt(it) }.toTypedArray()
        }
        val result = JSONObject()
        for (i in infos.indices) {
            val codecInfo = infos[i]
            val codec = JSONObject()
            val supportedTypes = codecInfo!!.supportedTypes
            codec.put("name", codecInfo.name)
                .put("isEncoder", codecInfo.isEncoder)
            val supportedTypesJson = JSONObject()
            for (type in supportedTypes) {
                supportedTypesJson.put(type, collectCapabilitiesForType(codecInfo, type))
            }
            codec.put("supportedTypes", supportedTypesJson)
            result.put(i.toString(), codec)
        }
        return result
    }

    /**
     * Retrieve capabilities (ColorFormats and CodecProfileLevels) for a specific codec type.
     *
     * @param codecInfo the currently inspected codec
     * @param type      supported type to collect
     * @return the color formats and codec profile levels available for a specific codec type.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Throws(JSONException::class)
    private fun collectCapabilitiesForType(codecInfo: MediaCodecInfo, type: String): JSONObject {
        val result = JSONObject()
        val codecCapabilities = codecInfo.getCapabilitiesForType(type)

        // Color Formats
        val colorFormats = codecCapabilities.colorFormats
        if (colorFormats.isNotEmpty()) {
            val colorFormatsJson = JSONArray()
            for (colorFormat in colorFormats) {
                colorFormatsJson.put(mColorFormatValues[colorFormat])
            }
            result.put("colorFormats", colorFormatsJson)
        }
        val codecType = identifyCodecType(codecInfo)

        // Profile Levels
        val codecProfileLevels = codecCapabilities.profileLevels
        if (codecProfileLevels.isNotEmpty()) {
            val profileLevels = JSONArray()
            for (codecProfileLevel in codecProfileLevels) {
                val profileValue = codecProfileLevel.profile
                val levelValue = codecProfileLevel.level
                if (codecType == null) {
                    // Unknown codec
                    profileLevels.put(profileValue + '-'.code + levelValue)
                    break
                }
                when (codecType) {
                    CodecType.AVC -> profileLevels.put("$profileValue${mAVCProfileValues[profileValue]}-${mAVCLevelValues[levelValue]}")
                    CodecType.H263 -> profileLevels.put("${mH263ProfileValues[profileValue]}-${mH263LevelValues[levelValue]}")
                    CodecType.MPEG4 -> profileLevels.put("${mMPEG4ProfileValues[profileValue]}-${mMPEG4LevelValues[levelValue]}")
                    CodecType.AAC -> profileLevels.put(mAACProfileValues[profileValue])
                }
            }
            result.put("profileLevels", profileLevels)
        }
        return result
    }

    /**
     * Looks for keywords in the codec name to identify its nature ([CodecType]).
     *
     * @param codecInfo the currently inspected codec
     * @return type of the codec or null if it could bot be guessed
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun identifyCodecType(codecInfo: MediaCodecInfo): CodecType? {
        val name = codecInfo.name
        for (token in AVC_TYPES) {
            if (name.contains(token)) {
                return CodecType.AVC
            }
        }
        for (token in H263_TYPES) {
            if (name.contains(token)) {
                return CodecType.H263
            }
        }
        for (token in MPEG4_TYPES) {
            if (name.contains(token)) {
                return CodecType.MPEG4
            }
        }
        for (token in AAC_TYPES) {
            if (name.contains(token)) {
                return CodecType.AAC
            }
        }
        return null
    }

    companion object {
        private const val COLOR_FORMAT_PREFIX = "COLOR_"
        private val MPEG4_TYPES = arrayOf("mp4", "mpeg4", "MP4", "MPEG4")
        private val AVC_TYPES = arrayOf("avc", "h264", "AVC", "H264")
        private val H263_TYPES = arrayOf("h263", "H263")
        private val AAC_TYPES = arrayOf("aac", "AAC")
    }
}