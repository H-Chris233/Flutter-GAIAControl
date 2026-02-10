/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

enum class StreamingStatisticsCodecs(val identifier: Int, val stringId: Int) {
    SBC(1, R.string.statistics_streaming_codec_sbc),
    AAC(2, R.string.statistics_streaming_codec_aac),
    APTX(3, R.string.statistics_streaming_codec_aptx),
    APTX_HD(4, R.string.statistics_streaming_codec_aptx_hd),
    APTX_ADAPTIVE(5, R.string.statistics_streaming_codec_aptx_adaptive);

    companion object {
        fun fromIdentifier(identifier: Int): StreamingStatisticsCodecs? {
            return values().firstOrNull { it.identifier == identifier }
        }
    }
}

enum class StreamingStatistics(
    override val identifier: Int,
    private val titleStringId: Int
) : CategoryStatisticSettings {
    CODEC(1, R.string.statistics_streaming_title_codec) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != 1) {
                return null;
            }
            val value = BytesUtils.getUINT8(data, 0)

            for (codec in StreamingStatisticsCodecs.values()) {
                if (value == codec.identifier) {
                    return context?.getString(codec.stringId)
                }
            }

            return null
        }
    },
    LOSSLESS_ENABLED(2, R.string.statistics_streaming_title_lossless_enabled) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != 1) {
                return null;
            }
            val value = BytesUtils.getUINT8(data, 0)
            return if (value == 0) {
                context?.getString(R.string.statistics_streaming_lossless_enabled_no)
            } else {
                context?.getString(R.string.statistics_streaming_lossless_enabled_yes)
            }
        }
    },
    BITRATE(3, R.string.statistics_streaming_title_bitrate) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != BytesUtils.INT32_BYTES_LENGTH) {
                return null;
            }
            val value = BytesUtils.getUINT32(data, 0) // value is bps
            val speed: Double = value.toDouble() / 1000.toDouble()
            return context?.getString(R.string.statistics_streaming_title_bitrate_format, speed) ?: value.toString()
        }
    },
    PRIMARY_RSSI(4, R.string.statistics_streaming_title_primary_rssi) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != BytesUtils.INT16_BYTES_LENGTH) {
                return null;
            }
            val value = BytesUtils.getSINT16(data, 0)
            return value.toString()
        }
    },
    PRIMARY_LINK_QUALITY(5, R.string.statistics_streaming_title_primary_link_quality) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != BytesUtils.INT16_BYTES_LENGTH) {
                return null;
            }
            val value = BytesUtils.getUINT16(data, 0)
            val percentage = (value.toDouble() / 65535) * 100
            return String.format("%.1f %%", percentage)
        }
    },
    TRANSPORT(6, R.string.statistics_streaming_title_transport) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != 1) {
                return null
            }
            val value = BytesUtils.getUINT8(data, 0)
            return when (value) {
                1 -> context?.getString(R.string.statistics_streaming_transport_p2p)
                2 -> context?.getString(R.string.statistics_streaming_transport_ble)
                3 -> context?.getString(R.string.statistics_streaming_transport_ap)
                else -> null
            }
        }
    },
    // Workaround for having the phones wifi rssi as a statistic
    WIFI_RSSI(999, R.string.statistics_streaming_title_wifi_rssi) {
        override fun getValue(context: Context?, data: ByteArray): String? {
            if (data.size != Int.SIZE_BYTES) {
                return null;
            }
            val value = BytesUtils.getSINT32(data, 0)
            return value.toString()
        }
    },
    ;

    override fun getTitle(context: Context?): String = context?.getString(titleStringId) ?: ""

    companion object {
        fun fromIdentifier(identifier: Int): StreamingStatistics? {
            return values().firstOrNull { it.identifier == identifier }
        }
    }
}
