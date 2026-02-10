/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
@file:Suppress("LocalVariableName")

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3

import android.util.Log
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.data.ACInfo
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FilterTopology
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.GainReduction
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGain
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainConfiguration
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainStep
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeftRightBalance
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Mode
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.NoiseIdCategory
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ScenarioConfiguration
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.State
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Support
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ToggleConfiguration
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.WindNoiseReduction
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.AudioCurationPlugin
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.AudioCurationPublisher
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger

private const val TAG = "V3AudioCurationPlugin"
private const val LOG_METHODS = DEBUG.QTIL.V3_AUDIO_CURATION_PLUGIN

class V3AudioCurationPlugin(sender: GaiaSender) : V3QTILPlugin(QTILFeature.AUDIO_CURATION, sender),
    AudioCurationPlugin {

    override val audioCurationPublisher = AudioCurationPublisher()

    override fun onStarted() {
        GaiaClientService.getPublicationManager().register(audioCurationPublisher)
    }

    override fun onStopped() {
        GaiaClientService.getPublicationManager().unregister(audioCurationPublisher)
    }

    override fun fetchInfo(info: ACInfo): Boolean {
        Logger.log(LOG_METHODS, TAG, "fetchInfo", Pair("info", info))

        return when (info) {
            ACInfo.MODES_COUNT -> {
                sendPacket(COMMANDS.V1_GET_MODES_COUNT)
                true
            }

            ACInfo.MODE -> {
                sendPacket(COMMANDS.V1_GET_CURRENT_MODE)
                true
            }

            ACInfo.FEED_FORWARD_GAIN -> {
                sendPacket(COMMANDS.V1_GET_FEED_FORWARD_GAIN)
                true
            }

            ACInfo.TOGGLES_COUNT -> {
                sendPacket(COMMANDS.V1_GET_TOGGLE_CONFIGURATION_COUNT)
                true
            }

            ACInfo.DEMO_SUPPORT -> {
                sendPacket(COMMANDS.V1_GET_DEMO_SUPPORT)
                true
            }

            ACInfo.DEMO_STATE -> {
                sendPacket(COMMANDS.V1_GET_DEMO_STATE)
                true
            }

            ACInfo.ADAPTATION_STATE -> {
                sendPacket(COMMANDS.V1_GET_ADAPTATION_STATE)
                true
            }

            ACInfo.LEAKTHROUGH_GAIN_CONFIGURATION -> {
                if (version < VERSIONS.V2) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2"
                    )
                    return false
                }
                sendPacket(COMMANDS.V2_GET_LEAKTHROUGH_GAIN_CONFIGURATION)
                true
            }

            ACInfo.LEAKTHROUGH_GAIN_STEP -> {
                if (version < VERSIONS.V2) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2"
                    )
                    return false
                }
                sendPacket(COMMANDS.V2_GET_LEAKTHROUGH_GAIN_STEP)
                true
            }

            ACInfo.LEFT_RIGHT_BALANCE -> {
                if (version < VERSIONS.V2) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2"
                    )
                    return false
                }
                sendPacket(COMMANDS.V2_GET_LEFT_RIGHT_BALANCE)
                true
            }

            ACInfo.WIND_NOISE_DETECTION_SUPPORT -> {
                if (version < VERSIONS.V2) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2"
                    )
                    return false
                }
                sendPacket(COMMANDS.V2_GET_WIND_NOISE_REDUCTION_SUPPORT)
                true
            }

            ACInfo.WIND_NOISE_DETECTION_STATE -> {
                if (version < VERSIONS.V2) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2"
                    )
                    return false
                }
                sendPacket(COMMANDS.V2_GET_WIND_NOISE_DETECTION_STATE)
                true
            }

            ACInfo.AUTO_TRANSPARENCY_SUPPORT -> {
                if (version < VERSIONS.V4) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 4"
                    )
                    return false
                }
                sendPacket(COMMANDS.V4_GET_AUTO_TRANSPARENCY_SUPPORT)
                true
            }

            ACInfo.AUTO_TRANSPARENCY_STATE -> {
                if (version < VERSIONS.V4) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 4"
                    )
                    return false
                }
                sendPacket(COMMANDS.V4_GET_AUTO_TRANSPARENCY_STATE)
                true
            }

            ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME -> {
                if (version < VERSIONS.V4) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 4"
                    )
                    return false
                }
                sendPacket(COMMANDS.V4_GET_AUTO_TRANSPARENCY_RELEASE_TIME)
                true
            }

            ACInfo.HOWLING_DETECTION_SUPPORT -> {
                if (version < VERSIONS.V5) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 5"
                    )
                    return false
                }
                sendPacket(COMMANDS.V5_GET_HOWLING_DETECTION_SUPPORT)
                true
            }

            ACInfo.HOWLING_DETECTION_STATE -> {
                if (version < VERSIONS.V5) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 5"
                    )
                    return false
                }
                sendPacket(COMMANDS.V5_GET_HOWLING_DETECTION_STATE)
                true
            }

            ACInfo.FEEDBACK_GAIN -> {
                if (version < VERSIONS.V5) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 5"
                    )
                    return false
                }
                sendPacket(COMMANDS.V5_GET_FEEDBACK_GAIN)
                true
            }

            ACInfo.NOISE_ID_SUPPORT -> {
                if (version < VERSIONS.V6) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 6"
                    )
                    return false
                }
                sendPacket(COMMANDS.V6_GET_NOISE_ID_SUPPORT)
                true
            }

            ACInfo.NOISE_ID_STATE -> {
                if (version < VERSIONS.V6) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 6"
                    )
                    return false
                }
                sendPacket(COMMANDS.V6_GET_NOISE_ID_STATE)
                true
            }

            ACInfo.NOISE_ID_CATEGORY -> {
                if (version < VERSIONS.V6) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 6"
                    )
                    return false
                }
                sendPacket(COMMANDS.V6_GET_NOISE_ID_CATEGORY)
                true
            }

            ACInfo.ADVERSE_ACOUSTIC_SUPPORT -> {
                if (version < VERSIONS.V7) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 7"
                    )
                    return false
                }
                sendPacket(COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_SUPPORT)
                true
            }

            ACInfo.ADVERSE_ACOUSTIC_STATE -> {
                if (version < VERSIONS.V7) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 7"
                    )
                    return false
                }
                sendPacket(COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_SUPPORT)
                true
            }

            ACInfo.FILTER_TOPOLOGY -> {
                if (version < VERSIONS.V8) {
                    Log.w(
                        TAG,
                        "[fetchInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 8"
                    )
                    return false
                }
                sendPacket(COMMANDS.V8_GET_FILTER_TOPOLOGY)
                true
            }

            ACInfo.TOGGLE_CONFIGURATION, ACInfo.SCENARIO_CONFIGURATION, ACInfo.AC_FEATURE_STATE -> {
                Log.w(TAG, "[fetchInfo] info=$info requires parameters: use #fetchInfo(info, parameter) instead")
                false
            }

            else -> {
                Log.w(TAG, "[fetchInfo] info $info is not fetchable.")
                false
            }
        }
    }

    override fun fetchInfo(info: ACInfo, parameter: Any): Boolean {
        Logger.log(LOG_METHODS, TAG, "fetchInfo", Pair("info", info), Pair("parameter", parameter))

        return when (info) {
            ACInfo.AC_FEATURE_STATE -> {
                if (parameter is FeatureState) {
                    sendPacket(COMMANDS.V1_GET_AC_STATE, parameter.feature.value)
                    return true
                }
                Log.w(TAG, "[fetchInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.TOGGLE_CONFIGURATION -> {
                if (parameter is ToggleConfiguration) {
                    sendPacket(COMMANDS.V1_GET_TOGGLE_CONFIGURATION, parameter.toggle)
                    return true
                }
                Log.w(TAG, "[fetchInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.SCENARIO_CONFIGURATION -> {
                if (parameter is ScenarioConfiguration) {
                    val scenario = parameter.scenario
                    return if (scenario != null && scenario.featureVersion <= version) {
                        sendPacket(COMMANDS.V1_GET_SCENARIO_CONFIGURATION, parameter.scenarioValue)
                        true
                    } else {
                        Log.w(
                            TAG,
                            "[fetchInfo] SCENARIO_CONFIGURATION: scenario=$scenario not supported for version=$version"
                        )
                        false
                    }
                }
                Log.w(TAG, "[fetchInfo] unexpected parameter type for info=$info")
                false
            }

            else -> {
                Log.i(TAG, "[fetchInfo] parameter is not needed for info=$info")
                fetchInfo(info)
                false
            }
        }
    }

    override fun setInfo(info: ACInfo, value: Any): Boolean {
        Logger.log(
            LOG_METHODS, TAG, "setInfo", Pair("version", version), Pair("info", info), Pair("value", value)
        )

        return when (info) {
            ACInfo.AC_FEATURE_STATE -> {
                if (value is FeatureState) {
                    sendPacket(COMMANDS.V1_SET_AC_STATE, value.setterBytes)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.MODE -> {
                if (value is Mode) {
                    // casting value into a byte
                    sendPacket(COMMANDS.V1_SET_MODE, value.value)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.LEAKTHROUGH_GAIN -> {
                if (value is LeakthroughGain) {
                    sendPacket(COMMANDS.V1_SET_LEAKTHROUGH_GAIN, value.bytes)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.TOGGLE_CONFIGURATION -> {
                if (value is ToggleConfiguration) {
                    sendPacket(COMMANDS.V1_SET_TOGGLE_CONFIGURATION, value.bytes)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.SCENARIO_CONFIGURATION -> {
                if (value is ScenarioConfiguration) {
                    sendPacket(COMMANDS.V1_SET_SCENARIO_CONFIGURATION, value.bytes)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.DEMO_STATE -> {
                if (value is DemoState) {
                    sendPacket(COMMANDS.V1_SET_DEMO_STATE, value.value)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.ADAPTATION_STATE -> {
                if (value is AdaptationState) {
                    sendPacket(COMMANDS.V1_SET_ADAPTATION, value.value)
                    return true
                }
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.LEAKTHROUGH_GAIN_STEP -> if (version < VERSIONS.V2) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2")
                false
            } else if (value is Int) {
                sendPacket(COMMANDS.V2_SET_LEAKTHROUGH_GAIN_STEP, value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.LEFT_RIGHT_BALANCE -> if (version < VERSIONS.V2) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2")
                false
            } else if (value is LeftRightBalance) {
                sendPacket(COMMANDS.V2_SET_LEFT_RIGHT_BALANCE, value.bytes)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.WIND_NOISE_DETECTION_STATE -> if (version < VERSIONS.V2) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 2")
                false
            } else if (value is State) {
                sendPacket(COMMANDS.V2_SET_WIND_NOISE_DETECTION_STATE, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.AUTO_TRANSPARENCY_STATE -> if (version < VERSIONS.V4) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 4")
                false
            } else if (value is State) {
                sendPacket(COMMANDS.V4_SET_AUTO_TRANSPARENCY_STATE, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME -> if (version < VERSIONS.V4) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 4")
                false
            } else if (value is AutoTransparencyReleaseTime) {
                sendPacket(COMMANDS.V4_SET_AUTO_TRANSPARENCY_RELEASE_TIME, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.HOWLING_DETECTION_STATE -> if (version < VERSIONS.V5) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 5")
                false
            } else if (value is State) {
                sendPacket(COMMANDS.V5_SET_HOWLING_DETECTION_STATE, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.NOISE_ID_STATE -> if (version < VERSIONS.V6) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 6")
                false
            } else if (value is State) {
                sendPacket(COMMANDS.V6_SET_NOISE_ID_STATE, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            ACInfo.ADVERSE_ACOUSTIC_STATE -> if (version < VERSIONS.V7) {
                Log.w(TAG, "[setInfo] info=$info not supported by device, requires AUDIO CURATION plugin version >= 7")
                false
            } else if (value is State) {
                sendPacket(COMMANDS.V7_SET_ADVERSE_ACOUSTIC_HANDLER_STATE, value.value)
                true
            } else {
                Log.w(TAG, "[setInfo] unexpected parameter type for info=$info")
                false
            }

            else -> {
                Log.w(TAG, "[setInfo] no setter for info=$info")
                false
            }
        }
    }

    override fun onNotification(packet: NotificationPacket) {
        Logger.log(LOG_METHODS, TAG, "onNotification", Pair("packet", packet))

        when (packet.command) {
            NOTIFICATIONS.V1_AC_STATE_CHANGE -> publishState(packet.data)
            NOTIFICATIONS.V1_MODE_CHANGE -> publishCurrentMode(packet.data)
            NOTIFICATIONS.V1_FEED_FORWARD_GAIN_CHANGE -> publishFeedForwardGain(packet.data)
            NOTIFICATIONS.V1_TOGGLE_CONFIGURATION -> publishToggleConfiguration(packet.data)
            NOTIFICATIONS.V1_SCENARIO_CONFIGURATION -> publishScenarioConfiguration(packet.data)
            NOTIFICATIONS.V1_DEMO_STATE -> publishDemoState(packet.data)
            NOTIFICATIONS.V1_ADAPTATION_STATE -> publishAdaptationState(packet.data)
            NOTIFICATIONS.V2_LEAKTHROUGH_GAIN_CONFIGURATION -> publishLeakthroughGainConfiguration(packet.data)
            NOTIFICATIONS.V2_LEAKTHROUGH_GAIN_STEP -> publishLeakthroughGainStep(packet.data)
            NOTIFICATIONS.V2_LEFT_RIGHT_BALANCE -> publishLeftRightBalance(packet.data)
            NOTIFICATIONS.V2_WIND_NOISE_DETECTION_STATE -> publishWindNoiseDetectionState(packet.data)
            NOTIFICATIONS.V2_WIND_NOISE_REDUCTION -> publishWindNoiseReduction(packet.data)
            NOTIFICATIONS.V4_AUTO_TRANSPARENCY_STATE_CHANGE -> publishAutoTransparencyState(packet.data)
            NOTIFICATIONS.V4_AUTO_TRANSPARENCY_RELEASE_TIME -> publishAutoTransparencyReleaseTime(packet.data)
            NOTIFICATIONS.V5_HOWLING_DETECTION_STATE_CHANGE -> publishHowlingDetectionState(packet.data)
            NOTIFICATIONS.V5_FEEDBACK_GAIN_CHANGE -> publishFeedbackGain(packet.data)
            NOTIFICATIONS.V6_NOISE_ID_STATE_CHANGE -> publishNoiseIdState(packet.data)
            NOTIFICATIONS.V6_NOISE_ID_CATEGORY_CHANGE -> publishNoiseIdCategory(packet.data)
            NOTIFICATIONS.V7_ADVERSE_ACOUSTIC_HANDLER_STATE_CHANGE -> publishAdverseAcousticState(packet.data)
            NOTIFICATIONS.V7_ADVERSE_ACOUSTIC_HANDLER_GAIN_REDUCTION_INDICATION ->
                publishAdverseAcousticGainReduction(packet.data)
            NOTIFICATIONS.V7_HCGR_GAIN_REDUCTION_INDICATION -> publishHowlingControlGainReduction(packet.data)
        }
    }

    override fun onResponse(response: ResponsePacket, sent: CommandPacket?) {
        Logger.log(
            LOG_METHODS, TAG, "onResponse",
            Pair("response", response), Pair("sent", sent)
        )

        when (response.command) {
            COMMANDS.V1_GET_AC_STATE -> publishState(response.data)
            COMMANDS.V1_GET_MODES_COUNT -> publishModesCount(response.data)
            COMMANDS.V1_GET_CURRENT_MODE -> publishCurrentMode(response.data)
            COMMANDS.V1_GET_FEED_FORWARD_GAIN -> publishFeedForwardGain(response.data)
            COMMANDS.V1_GET_TOGGLE_CONFIGURATION_COUNT -> publishTogglesCount(response.data)
            COMMANDS.V1_GET_TOGGLE_CONFIGURATION -> publishToggleConfiguration(response.data)
            COMMANDS.V1_GET_SCENARIO_CONFIGURATION -> publishScenarioConfiguration(response.data)
            COMMANDS.V1_GET_DEMO_SUPPORT -> publishDemoSupport(response.data)
            COMMANDS.V1_GET_DEMO_STATE -> publishDemoState(response.data)
            COMMANDS.V1_GET_ADAPTATION_STATE -> publishAdaptationState(response.data)
            COMMANDS.V2_GET_LEAKTHROUGH_GAIN_CONFIGURATION -> publishLeakthroughGainConfiguration(response.data)
            COMMANDS.V2_GET_LEAKTHROUGH_GAIN_STEP -> publishLeakthroughGainStep(response.data)
            COMMANDS.V2_GET_LEFT_RIGHT_BALANCE -> publishLeftRightBalance(response.data)
            COMMANDS.V2_GET_WIND_NOISE_REDUCTION_SUPPORT -> publishWindNoiseDetectionSupport(response.data)
            COMMANDS.V2_GET_WIND_NOISE_DETECTION_STATE -> publishWindNoiseDetectionState(response.data)
            COMMANDS.V4_GET_AUTO_TRANSPARENCY_SUPPORT -> publishAutoTransparencySupport(response.data)
            COMMANDS.V4_GET_AUTO_TRANSPARENCY_STATE -> publishAutoTransparencyState(response.data)
            COMMANDS.V4_GET_AUTO_TRANSPARENCY_RELEASE_TIME -> publishAutoTransparencyReleaseTime(response.data)
            COMMANDS.V5_GET_HOWLING_DETECTION_SUPPORT -> publishHowlingDetectionSupport(response.data)
            COMMANDS.V5_GET_HOWLING_DETECTION_STATE -> publishHowlingDetectionState(response.data)
            COMMANDS.V5_GET_FEEDBACK_GAIN -> publishFeedbackGain(response.data)
            COMMANDS.V6_GET_NOISE_ID_SUPPORT -> publishNoiseIdSupport(response.data)
            COMMANDS.V6_GET_NOISE_ID_STATE -> publishNoiseIdState(response.data)
            COMMANDS.V6_GET_NOISE_ID_CATEGORY -> publishNoiseIdCategory(response.data)
            COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_SUPPORT -> publishAdverseAcousticSupport(response.data)
            COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_STATE -> publishAdverseAcousticState(response.data)
            COMMANDS.V8_GET_FILTER_TOPOLOGY -> publishFilterTopology(response.data)
        }
    }

    override fun onError(errorPacket: ErrorPacket, sent: CommandPacket?) {
        Logger.log(LOG_METHODS, TAG, "onError", Pair("packet", errorPacket), Pair("sent", sent))
        val status = errorPacket.v3ErrorStatus
        onError(errorPacket.command, Reason.valueOf(status))
    }

    override fun onFailed(source: GaiaPacket, reason: Reason) {
        Logger.log(LOG_METHODS, TAG, "onFailed", Pair("reason", reason), Pair("packet", source))

        if (source !is V3Packet) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.")
            return
        }

        onError(source.command, reason)
    }

    private fun onError(command: Int, reason: Reason) {
        when (command) {
            COMMANDS.V1_GET_AC_STATE, COMMANDS.V1_SET_AC_STATE ->
                audioCurationPublisher.publishError(ACInfo.AC_FEATURE_STATE, reason)

            COMMANDS.V1_GET_MODES_COUNT ->
                audioCurationPublisher.publishError(ACInfo.MODES_COUNT, reason)

            COMMANDS.V1_GET_CURRENT_MODE, COMMANDS.V1_SET_MODE ->
                audioCurationPublisher.publishError(ACInfo.MODE, reason)

            COMMANDS.V1_GET_FEED_FORWARD_GAIN ->
                audioCurationPublisher.publishError(ACInfo.FEED_FORWARD_GAIN, reason)

            COMMANDS.V1_SET_LEAKTHROUGH_GAIN ->
                audioCurationPublisher.publishError(ACInfo.LEAKTHROUGH_GAIN, reason)

            COMMANDS.V1_GET_TOGGLE_CONFIGURATION_COUNT ->
                audioCurationPublisher.publishError(ACInfo.TOGGLES_COUNT, reason)

            COMMANDS.V1_GET_TOGGLE_CONFIGURATION, COMMANDS.V1_SET_TOGGLE_CONFIGURATION ->
                audioCurationPublisher.publishError(ACInfo.TOGGLE_CONFIGURATION, reason)

            COMMANDS.V1_GET_SCENARIO_CONFIGURATION, COMMANDS.V1_SET_SCENARIO_CONFIGURATION ->
                audioCurationPublisher.publishError(ACInfo.SCENARIO_CONFIGURATION, reason)

            COMMANDS.V1_GET_DEMO_SUPPORT -> audioCurationPublisher.publishError(ACInfo.DEMO_SUPPORT, reason)

            COMMANDS.V1_GET_DEMO_STATE, COMMANDS.V1_SET_DEMO_STATE ->
                audioCurationPublisher.publishError(ACInfo.DEMO_STATE, reason)

            COMMANDS.V1_GET_ADAPTATION_STATE, COMMANDS.V1_SET_ADAPTATION ->
                audioCurationPublisher.publishError(ACInfo.ADAPTATION_STATE, reason)

            COMMANDS.V2_GET_LEAKTHROUGH_GAIN_CONFIGURATION ->
                audioCurationPublisher.publishError(ACInfo.LEAKTHROUGH_GAIN_CONFIGURATION, reason)

            COMMANDS.V2_GET_LEAKTHROUGH_GAIN_STEP, COMMANDS.V2_SET_LEAKTHROUGH_GAIN_STEP ->
                audioCurationPublisher.publishError(ACInfo.LEAKTHROUGH_GAIN_STEP, reason)

            COMMANDS.V2_GET_LEFT_RIGHT_BALANCE, COMMANDS.V2_SET_LEFT_RIGHT_BALANCE ->
                audioCurationPublisher.publishError(ACInfo.LEFT_RIGHT_BALANCE, reason)

            COMMANDS.V2_GET_WIND_NOISE_REDUCTION_SUPPORT ->
                audioCurationPublisher.publishError(ACInfo.WIND_NOISE_DETECTION_SUPPORT, reason)

            COMMANDS.V2_GET_WIND_NOISE_DETECTION_STATE, COMMANDS.V2_SET_WIND_NOISE_DETECTION_STATE ->
                audioCurationPublisher.publishError(ACInfo.WIND_NOISE_DETECTION_STATE, reason)

            COMMANDS.V4_GET_AUTO_TRANSPARENCY_SUPPORT ->
                audioCurationPublisher.publishError(ACInfo.AUTO_TRANSPARENCY_SUPPORT, reason)

            COMMANDS.V4_GET_AUTO_TRANSPARENCY_STATE, COMMANDS.V4_SET_AUTO_TRANSPARENCY_STATE ->
                audioCurationPublisher.publishError(ACInfo.AUTO_TRANSPARENCY_STATE, reason)

            COMMANDS.V4_GET_AUTO_TRANSPARENCY_RELEASE_TIME, COMMANDS.V4_SET_AUTO_TRANSPARENCY_RELEASE_TIME ->
                audioCurationPublisher.publishError(ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME, reason)

            COMMANDS.V5_GET_HOWLING_DETECTION_SUPPORT ->
                audioCurationPublisher.publishError(ACInfo.HOWLING_DETECTION_SUPPORT, reason)

            COMMANDS.V5_GET_HOWLING_DETECTION_STATE, COMMANDS.V5_SET_HOWLING_DETECTION_STATE ->
                audioCurationPublisher.publishError(ACInfo.HOWLING_DETECTION_STATE, reason)

            COMMANDS.V5_GET_FEEDBACK_GAIN -> audioCurationPublisher.publishError(ACInfo.FEEDBACK_GAIN, reason)

            COMMANDS.V6_GET_NOISE_ID_SUPPORT -> audioCurationPublisher.publishError(ACInfo.NOISE_ID_SUPPORT, reason)

            COMMANDS.V6_GET_NOISE_ID_STATE, COMMANDS.V6_SET_NOISE_ID_STATE ->
                audioCurationPublisher.publishError(ACInfo.NOISE_ID_STATE, reason)

            COMMANDS.V6_GET_NOISE_ID_CATEGORY -> audioCurationPublisher.publishError(ACInfo.NOISE_ID_CATEGORY, reason)

            COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_SUPPORT ->
                audioCurationPublisher.publishError(ACInfo.ADVERSE_ACOUSTIC_SUPPORT, reason)

            COMMANDS.V7_GET_ADVERSE_ACOUSTIC_HANDLER_STATE, COMMANDS.V7_SET_ADVERSE_ACOUSTIC_HANDLER_STATE ->
                audioCurationPublisher.publishError(ACInfo.ADVERSE_ACOUSTIC_STATE, reason)
        }
    }

    private fun publishState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishState", Pair("data", data))
        val state = FeatureState(data)
        audioCurationPublisher.publishState(state)
    }

    private fun publishCurrentMode(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishCurrentMode", Pair("data", data))
        val mode = Mode(version, data)
        audioCurationPublisher.publishCurrentMode(mode)
    }

    private fun publishFeedForwardGain(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishFeedForwardGain", Pair("data", data))
        val gain = Gain(version, data)
        audioCurationPublisher.publishFeedForwardGain(gain)
    }

    private fun publishModesCount(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishModesCount", Pair("data", data))
        val COUNT_OFFSET = 0
        val count = BytesUtils.getUINT8(data, COUNT_OFFSET)
        audioCurationPublisher.publishModesCount(count)
    }

    private fun publishTogglesCount(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishTogglesCount", Pair("data", data))
        val COUNT_OFFSET = 0
        val count = BytesUtils.getUINT8(data, COUNT_OFFSET)
        audioCurationPublisher.publishTogglesCount(count)
    }

    private fun publishToggleConfiguration(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishToggleConfiguration", Pair("data", data))
        val configuration = ToggleConfiguration(data)
        audioCurationPublisher.publishToggleConfiguration(configuration)
    }

    private fun publishScenarioConfiguration(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishScenarioConfiguration", Pair("data", data))
        val configuration = ScenarioConfiguration(data)
        audioCurationPublisher.publishScenarioConfiguration(configuration)
    }

    private fun publishDemoSupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishDemoSupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishDemoSupport(support)
    }

    private fun publishDemoState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishDemoState", Pair("data", data))
        val MODE_OFFSET = 0
        val state = DemoState.valueOf(BytesUtils.getUINT8(data, MODE_OFFSET))
        audioCurationPublisher.publishDemoState(state)
    }

    private fun publishAdaptationState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAdaptationState", Pair("data", data))
        val STATUS_OFFSET = 0
        val state = AdaptationState.valueOf(BytesUtils.getUINT8(data, STATUS_OFFSET))
        audioCurationPublisher.publishAdaptationState(state)
    }

    private fun publishLeakthroughGainConfiguration(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishLeakthroughGainConfiguration", Pair("data", data))
        val configuration = LeakthroughGainConfiguration(data)
        audioCurationPublisher.publishLeakthroughGainConfiguration(configuration)
    }

    private fun publishLeakthroughGainStep(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishLeakthroughGainStep", Pair("data", data))
        val step = LeakthroughGainStep(data)
        audioCurationPublisher.publishLeakthroughGainStep(step)
    }

    private fun publishLeftRightBalance(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishLeftRightBalance", Pair("data", data))
        val balance = LeftRightBalance(data)
        audioCurationPublisher.publishLeftRightBalance(balance)
    }

    private fun publishWindNoiseDetectionSupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishWindNoiseDetectionSupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishWindNoiseDetectionSupport(support)
    }

    private fun publishWindNoiseDetectionState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishWindNoiseDetectionState", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val state = State.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishWindNoiseDetectionState(state)
    }

    private fun publishWindNoiseReduction(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishWindNoiseReduction", Pair("data", data))
        val reduction = WindNoiseReduction(data)
        audioCurationPublisher.publishWindNoiseReduction(reduction)
    }

    private fun publishAutoTransparencySupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAutoTransparencySupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishAutoTransparencySupport(support)
    }

    private fun publishAutoTransparencyState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAutoTransparencyState", Pair("data", data))
        val STATE_OFFSET = 0
        val state = State.valueOf(BytesUtils.getUINT8(data, STATE_OFFSET))
        audioCurationPublisher.publishAutoTransparencyState(state)
    }

    private fun publishAutoTransparencyReleaseTime(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAutoTransparencyReleaseTime", Pair("data", data))
        val TIME_OFFSET = 0
        val time = AutoTransparencyReleaseTime.valueOf(BytesUtils.getUINT8(data, TIME_OFFSET))
        audioCurationPublisher.publishAutoTransparencyReleaseTime(time)
    }

    private fun publishHowlingDetectionSupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishHowlingDetectionSupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishHowlingDetectionSupport(support)
    }

    private fun publishHowlingDetectionState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishHowlingDetectionState", Pair("data", data))
        val STATE_OFFSET = 0
        val state = State.valueOf(BytesUtils.getUINT8(data, STATE_OFFSET))
        audioCurationPublisher.publishHowlingDetectionState(state)
    }

    private fun publishFeedbackGain(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishFeedbackGain", Pair("data", data))
        val gain = Gain(version, data)
        audioCurationPublisher.publishFeedbackGain(gain)
    }

    private fun publishNoiseIdSupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishNoiseIdSupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishNoiseIdSupport(support)
    }

    private fun publishNoiseIdState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishNoiseIdState", Pair("data", data))
        val STATE_OFFSET = 0
        val state = State.valueOf(BytesUtils.getUINT8(data, STATE_OFFSET))
        audioCurationPublisher.publishNoiseIdState(state)
    }

    private fun publishNoiseIdCategory(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishNoiseIdCategory", Pair("data", data))
        val CATEGORY_OFFSET = 0
        val category = NoiseIdCategory.valueOf(BytesUtils.getUINT8(data, CATEGORY_OFFSET))
        audioCurationPublisher.publishNoiseIdCategory(category)
    }

    private fun publishAdverseAcousticSupport(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAdverseAcousticSupport", Pair("data", data))
        val SUPPORT_OFFSET = 0
        val support = Support.valueOf(BytesUtils.getUINT8(data, SUPPORT_OFFSET))
        audioCurationPublisher.publishAdverseAcousticSupport(support)
    }

    private fun publishAdverseAcousticState(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAdverseAcousticState", Pair("data", data))
        val STATE_OFFSET = 0
        val state = State.valueOf(BytesUtils.getUINT8(data, STATE_OFFSET))
        audioCurationPublisher.publishAdverseAcousticState(state)
    }

    private fun publishAdverseAcousticGainReduction(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishAdverseAcousticGainReduction", Pair("data", data))
        val reduction = GainReduction(data)
        audioCurationPublisher.publishAdverseAcousticGainReduction(reduction)
    }

    private fun publishHowlingControlGainReduction(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishHowlingControlGainReduction", Pair("data", data))
        val reduction = GainReduction(data)
        audioCurationPublisher.publishHowlingControlGainReduction(reduction)
    }

    private fun publishFilterTopology(data: ByteArray) {
        Logger.log(LOG_METHODS, TAG, "publishFilterTopology", Pair("data", data))
        val TOPOLOGY_OFFSET = 0
        val topology = FilterTopology.valueOf(BytesUtils.getUINT8(data, TOPOLOGY_OFFSET))
        audioCurationPublisher.publishFilterTopology(topology)
    }

    private object COMMANDS {
        const val V1_GET_AC_STATE = 0x00
        const val V1_SET_AC_STATE = 0x01
        const val V1_GET_MODES_COUNT = 0x02
        const val V1_GET_CURRENT_MODE = 0x03
        const val V1_SET_MODE = 0x04
        const val V1_GET_FEED_FORWARD_GAIN = 0x05
        const val V1_SET_LEAKTHROUGH_GAIN = 0x06
        const val V1_GET_TOGGLE_CONFIGURATION_COUNT = 0x07
        const val V1_GET_TOGGLE_CONFIGURATION = 0x08
        const val V1_SET_TOGGLE_CONFIGURATION = 0x09
        const val V1_GET_SCENARIO_CONFIGURATION = 0x0A
        const val V1_SET_SCENARIO_CONFIGURATION = 0x0B
        const val V1_GET_DEMO_SUPPORT = 0x0C
        const val V1_GET_DEMO_STATE = 0x0D
        const val V1_SET_DEMO_STATE = 0x0E
        const val V1_GET_ADAPTATION_STATE = 0x0F
        const val V1_SET_ADAPTATION = 0x10
        const val V2_GET_LEAKTHROUGH_GAIN_CONFIGURATION = 0x11
        const val V2_GET_LEAKTHROUGH_GAIN_STEP = 0x12
        const val V2_SET_LEAKTHROUGH_GAIN_STEP = 0x13
        const val V2_GET_LEFT_RIGHT_BALANCE = 0x14
        const val V2_SET_LEFT_RIGHT_BALANCE = 0x15
        const val V2_GET_WIND_NOISE_REDUCTION_SUPPORT = 0x16
        const val V2_GET_WIND_NOISE_DETECTION_STATE = 0x17
        const val V2_SET_WIND_NOISE_DETECTION_STATE = 0x18
        const val V4_GET_AUTO_TRANSPARENCY_SUPPORT = 0x19
        const val V4_GET_AUTO_TRANSPARENCY_STATE = 0x1A
        const val V4_SET_AUTO_TRANSPARENCY_STATE = 0x1B
        const val V4_GET_AUTO_TRANSPARENCY_RELEASE_TIME = 0x1C
        const val V4_SET_AUTO_TRANSPARENCY_RELEASE_TIME = 0x1D
        const val V5_GET_HOWLING_DETECTION_SUPPORT = 0x1E
        const val V5_GET_HOWLING_DETECTION_STATE = 0x1F
        const val V5_SET_HOWLING_DETECTION_STATE = 0x20
        const val V5_GET_FEEDBACK_GAIN = 0x21
        const val V6_GET_NOISE_ID_SUPPORT = 0x22
        const val V6_GET_NOISE_ID_STATE = 0x23
        const val V6_SET_NOISE_ID_STATE = 0x24
        const val V6_GET_NOISE_ID_CATEGORY = 0x25
        const val V7_GET_ADVERSE_ACOUSTIC_HANDLER_SUPPORT = 0x26
        const val V7_GET_ADVERSE_ACOUSTIC_HANDLER_STATE = 0x27
        const val V7_SET_ADVERSE_ACOUSTIC_HANDLER_STATE = 0x28
        const val V8_GET_FILTER_TOPOLOGY = 0x29
    }

    private object NOTIFICATIONS {
        const val V1_AC_STATE_CHANGE = 0x00
        const val V1_MODE_CHANGE = 0x01
        const val V1_FEED_FORWARD_GAIN_CHANGE = 0x02
        const val V1_TOGGLE_CONFIGURATION = 0x03
        const val V1_SCENARIO_CONFIGURATION = 0x04
        const val V1_DEMO_STATE = 0x05
        const val V1_ADAPTATION_STATE = 0x06
        const val V2_LEAKTHROUGH_GAIN_CONFIGURATION = 0x07
        const val V2_LEAKTHROUGH_GAIN_STEP = 0x08
        const val V2_LEFT_RIGHT_BALANCE = 0x09
        const val V2_WIND_NOISE_DETECTION_STATE = 0x0A
        const val V2_WIND_NOISE_REDUCTION = 0x0B
        const val V4_AUTO_TRANSPARENCY_STATE_CHANGE = 0x0C
        const val V4_AUTO_TRANSPARENCY_RELEASE_TIME = 0x0D
        const val V5_HOWLING_DETECTION_STATE_CHANGE = 0x0E
        const val V5_FEEDBACK_GAIN_CHANGE = 0x0F
        const val V6_NOISE_ID_STATE_CHANGE = 0x10
        const val V6_NOISE_ID_CATEGORY_CHANGE = 0x11
        const val V7_ADVERSE_ACOUSTIC_HANDLER_STATE_CHANGE = 0x12
        const val V7_ADVERSE_ACOUSTIC_HANDLER_GAIN_REDUCTION_INDICATION = 0x13
        const val V7_HCGR_GAIN_REDUCTION_INDICATION = 0x14

    }

    object VERSIONS {
        const val V1 = 0x01
        const val V2 = 0x02
        const val V3 = 0x03
        const val V4 = 0x04
        const val V5 = 0x05
        const val V6 = 0x06
        const val V7 = 0x07
        const val V8 = 0x08
    }

}
