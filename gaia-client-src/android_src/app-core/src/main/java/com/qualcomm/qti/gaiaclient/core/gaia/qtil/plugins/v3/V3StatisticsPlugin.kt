/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3

import android.util.Log
import androidx.annotation.Nullable
import com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo
import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticDescriptor
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.StatisticsPlugin
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.StatisticsPublisher
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import java.lang.Integer.min

private const val TAG = "V3StatisticsPlugin"

class V3StatisticsPlugin constructor(
    sender: GaiaSender,
    private val publicationManager: PublicationManager
) : V3QTILPlugin(QTILFeature.STATISTICS, sender), StatisticsPlugin {

    constructor(sender: GaiaSender) : this(sender, getPublicationManager())

    private val statisticsPublisher = StatisticsPublisher()
    private var categories = mutableListOf<CategoryID>()

    override fun onStarted() {
        publicationManager.register(statisticsPublisher)
    }

    override fun onStopped() {
        publicationManager.unregister(statisticsPublisher)
    }

    override fun getStatisticsPublisher(): StatisticsPublisher {
        return statisticsPublisher
    }

    override fun fetchInfo(info: StatisticsInfo, parameter: Any?): Boolean {
        when (info) {
            StatisticsInfo.ALL_STATISTICS_IN_CATEGORY -> {
                if (parameter is CategoryID) {
                    fetchAllStatistics(parameter)
                } else {
                    Log.w(TAG, "[fetchInfo] ${info.name} should have a CategoryID parameter")
                    statisticsPublisher.publishError(info, Reason.MALFORMED_REQUEST)
                    return false
                }
            }
            StatisticsInfo.STATISTIC_VALUES -> {
                if (parameter is List<*>) {
                    fetchStatisticsValues(parameter.filterIsInstance<StatisticDescriptor>())
                } else {
                    Log.w(TAG, "[fetchInfo] ${info.name} should have a List parameter")
                    statisticsPublisher.publishError(info, Reason.MALFORMED_REQUEST)
                    return false
                }
            }
            StatisticsInfo.CATEGORIES -> {
                if (parameter == null) {
                    fetchCategories()
                } else {
                    Log.w(TAG, "[fetchInfo] ${info.name} should not have a parameter")
                    statisticsPublisher.publishError(info, Reason.MALFORMED_REQUEST)
                    return false
                }
            }
        }
        return true
    }

    private fun fetchCategories() {
        categories.clear()
        sendGetCategories(CategoryID(0))
    }

    private fun fetchAllStatistics(categoryID: CategoryID) {
        sendGetAllStatistics(categoryID, StatisticID(0))
    }

    private fun fetchStatisticsValues(descriptors: List<StatisticDescriptor>) {
        val maxLengthReceive = getMaxPayloadSize(SizeInfo.OPTIMUM_TX_PAYLOAD)
        val RESPONSE_FIXED_LENGTH = 1
        val MAX_RESPONSE_PACKET_LENGTH = 9
        val maxPacketsReceive = (maxLengthReceive - RESPONSE_FIXED_LENGTH) / MAX_RESPONSE_PACKET_LENGTH // Round down

        val maxLengthSend = getMaxPayloadSize(SizeInfo.OPTIMUM_RX_PAYLOAD)
        val SEND_FIXED_LENGTH = 0
        val MAX_SEND_PACKET_LENGTH = 3
        val maxPacketsSend = (maxLengthSend - SEND_FIXED_LENGTH) / MAX_SEND_PACKET_LENGTH // Round down

        val maxPerRequest = min(maxPacketsReceive, maxPacketsSend)

        var numberInCurrentRequest = 0
        var payloads = mutableListOf<ByteArray>()
        var currentPayload = ByteArray(0)

        for (descriptor in descriptors) {
            currentPayload += descriptor.toByteArray()
            numberInCurrentRequest += 1
            if (numberInCurrentRequest == maxPerRequest) {
                payloads.add(currentPayload)
                numberInCurrentRequest = 0
                currentPayload = ByteArray(0)
            }
        }

        if (currentPayload.isNotEmpty()) {
            payloads.add(currentPayload)
        }

        for (payload in payloads) {
            sendPacket(COMMANDS.V1_GET_STATISTICS_VALUES, payload)
        }
    }

    private fun sendGetCategories(lastCategoryID: CategoryID) {
        val parameters = ByteArray(BytesUtils.INT16_BYTES_LENGTH)
        BytesUtils.setUINT16(lastCategoryID.value, parameters, 0)
        sendPacket(COMMANDS.V1_GET_ALL_CATEGORIES, parameters)
    }

    private fun sendGetAllStatistics(categoryID: CategoryID, lastStatisticID: StatisticID) {
        val parameters = ByteArray(BytesUtils.INT16_BYTES_LENGTH + 1)
        BytesUtils.setUINT16(categoryID.value, parameters, 0)
        BytesUtils.setUINT8(lastStatisticID.value, parameters, BytesUtils.INT16_BYTES_LENGTH)
        sendPacket(COMMANDS.V1_GET_ALL_STATISTICS_IN_CATEGORY, parameters)
    }

    override fun onResponse(response: ResponsePacket, @Nullable sent: CommandPacket?) {
        when (response.command) {
            COMMANDS.V1_GET_ALL_CATEGORIES -> processCategories(response.data)
            COMMANDS.V1_GET_ALL_STATISTICS_IN_CATEGORY -> processGetAllStats(response.data)
            COMMANDS.V1_GET_STATISTICS_VALUES -> processGetStatsValues(response.data)
        }
    }

    override fun onNotification(packet: NotificationPacket) {
        // There are no notifications
    }

    override fun onError(errorPacket: ErrorPacket, @Nullable sent: CommandPacket?) {
        val status = errorPacket.v3ErrorStatus
        when (errorPacket.command) {
            COMMANDS.V1_GET_ALL_CATEGORIES -> statisticsPublisher.publishError(
                StatisticsInfo.CATEGORIES,
                Reason.valueOf(status)
            )
            COMMANDS.V1_GET_ALL_STATISTICS_IN_CATEGORY -> statisticsPublisher.publishError(
                StatisticsInfo.ALL_STATISTICS_IN_CATEGORY,
                Reason.valueOf(status)
            )
            COMMANDS.V1_GET_STATISTICS_VALUES -> statisticsPublisher.publishError(
                StatisticsInfo.STATISTIC_VALUES,
                Reason.valueOf(status)
            )
        }
    }

    override fun onFailed(source: GaiaPacket?, reason: Reason?) {
        if (source !is V3Packet) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.")
            return
        }
        when (source.command) {
            COMMANDS.V1_GET_ALL_CATEGORIES -> statisticsPublisher.publishError(
                StatisticsInfo.CATEGORIES,
                reason
            )
            COMMANDS.V1_GET_ALL_STATISTICS_IN_CATEGORY -> statisticsPublisher.publishError(
                StatisticsInfo.ALL_STATISTICS_IN_CATEGORY,
                reason
            )
            COMMANDS.V1_GET_STATISTICS_VALUES -> statisticsPublisher.publishError(
                StatisticsInfo.STATISTIC_VALUES,
                reason
            )
        }
    }

    private fun publishResponseLengthError(info: StatisticsInfo, data: ByteArray) {
        Log.w(TAG, "[notifyResponseLengthError] ${info.name} Data length error length: ${data.size}")
        statisticsPublisher.publishError(info, Reason.MALFORMED_RESPONSE)
        return
    }

    private fun processCategories(data: ByteArray) {
        if (data.size % 2 == 0) {
            publishResponseLengthError(StatisticsInfo.CATEGORIES, data)
            return
        }
        val moreComing = BytesUtils.getUINT8(data, 0) > 0

        var currentLast: Int = 0
        for (offset in 1 until data.size step BytesUtils.INT16_BYTES_LENGTH) {
            val category = BytesUtils.getUINT16(data, offset)
            if (category != 0 && category > currentLast) {
                currentLast = category
                categories.add(CategoryID(category))
            }
        }
        if (moreComing && currentLast > 0) {
            sendGetCategories(CategoryID(currentLast))
        } else {
            statisticsPublisher.publishCategories(categories)
        }
    }

    private fun processGetAllStats(data: ByteArray) {
        val MIN_SIZE_FOR_ENTRY = 3

        if (data.size < MIN_SIZE_FOR_ENTRY) {
            publishResponseLengthError(StatisticsInfo.ALL_STATISTICS_IN_CATEGORY, data)
            return
        }

        val moreComing = BytesUtils.getUINT8(data, 0) > 0
        val category: Int = BytesUtils.getUINT16(data, 1)
        val categoryID = CategoryID(category)

        var currentLast: Int = 0
        var offset = 1 + BytesUtils.INT16_BYTES_LENGTH

        val statisticValues = mutableListOf<StatisticValue>()

        while ((offset + MIN_SIZE_FOR_ENTRY) <= data.size) {
            val statistic = BytesUtils.getUINT8(data, offset)
            // Next byte is reserved for flags that we currently don't use
            val length = BytesUtils.getUINT8(data, offset + 2)
            var value = ByteArray(0)
            if (offset + MIN_SIZE_FOR_ENTRY + length <= data.size) {
                value = data.copyOfRange(offset + MIN_SIZE_FOR_ENTRY, offset + MIN_SIZE_FOR_ENTRY + length)
            }

            if (statistic > currentLast) {
                currentLast = statistic
                val publishedValue = StatisticValue(StatisticDescriptor(categoryID, StatisticID(statistic)), value)
                statisticValues.add(publishedValue)
            }
            offset += MIN_SIZE_FOR_ENTRY + length
        }

        statisticsPublisher.publishStatistics(mapOf(categoryID to statisticValues))

        if (moreComing && currentLast > 0) {
            sendGetAllStatistics(categoryID, StatisticID(currentLast))
        }
    }

    private fun processGetStatsValues(data: ByteArray) {
        if (data.isEmpty()) {
            publishResponseLengthError(StatisticsInfo.STATISTIC_VALUES, data)
            return
        }

        val MIN_SIZE_FOR_ENTRY = 5
        // Currently ignoring more coming byte
        var offset = 1

        val statisticValues = mutableListOf<StatisticValue>()

        while ((offset + MIN_SIZE_FOR_ENTRY) <= data.size) {
            val category = BytesUtils.getUINT16(data, offset)
            val statistic = BytesUtils.getUINT8(data, offset + BytesUtils.INT16_BYTES_LENGTH)
            // Currently ignoring flags byte
            val length = BytesUtils.getUINT8(data, offset + BytesUtils.INT16_BYTES_LENGTH + 2)
            var value = ByteArray(0)
            if (offset + MIN_SIZE_FOR_ENTRY + length <= data.size) {
                value = data.copyOfRange(offset + MIN_SIZE_FOR_ENTRY, offset + MIN_SIZE_FOR_ENTRY + length)
            }

            val publishedValue =
                StatisticValue(StatisticDescriptor(CategoryID(category), StatisticID(statistic)), value)
            statisticValues.add(publishedValue)

            offset += MIN_SIZE_FOR_ENTRY + length
        }

        statisticsPublisher.publishStatistics(statisticValues.groupBy { it.descriptor.category })
    }

    private object COMMANDS {
        const val V1_GET_ALL_CATEGORIES = 0x00
        const val V1_GET_ALL_STATISTICS_IN_CATEGORY = 0x01
        const val V1_GET_STATISTICS_VALUES = 0x02
    }

    private object NOTIFICATIONS {
    }
}
