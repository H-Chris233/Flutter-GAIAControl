/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt

/**
 * This class allows to build understandable statuses for all status returned when using
 * [android.bluetooth.BluetoothGatt] and [android.bluetooth.BluetoothGattCallback].
 *
 * Note: all [BluetoothGatt] specific error codes are covered by either [ConnectionStatus], [HciStatus] or [AttStatus]:
 * https://developer.android.com/reference/android/bluetooth/BluetoothGatt
 */
abstract class GattStatus(val value: Int) {
    abstract val prefix: String
    abstract val details: String

    val name: String = javaClass.simpleName

    val shortLabel: String by lazy { "$name(${value.toHex()})" }

    val label: String by lazy { "$prefix.$shortLabel" }

    fun message(detailed: Boolean): String =
        if (detailed && details.isNotEmpty()) "$label: $details" else label

    override fun toString(): String {
        return label
    }
}

private fun Int.toHex(): String = "0x${this.toUInt().toString(16).uppercase()}"

/**
 * This class groups all error statuses from the ATT definition as defined in the Bluetooth Core Specification
 * 5.4 Vol3 PartF 3.4.1.
 */
sealed class AttStatus(value: Int, override val details: String = "") : GattStatus(value) {
    override val prefix: String = "ATT"

    // ATT error codes from Bluetooth Core Specification 5.4,  Volume 3, Part F, Section 3.4.1.
    object INVALID_HANDLE : AttStatus(0x01, "The attribute handle given was not valid on this server.")
    object READ_NOT_PERMITTED : AttStatus(0x02, "The attribute cannot be read.")
    object WRITE_NOT_PERMITTED : AttStatus(0x03, "The attribute cannot be written.")
    object INVALID_PDU : AttStatus(0x04, "The attribute PDU was invalid.")
    object INSUFFICIENT_AUTHENTICATION :
        AttStatus(0x05, "The attribute requires authentication before it can be read or written.")

    object REQUEST_NOT_SUPPORTED :
        AttStatus(0x06, "Attribute server does not support the request received from the client.")

    object INVALID_OFFSET : AttStatus(0x07, "Offset specified was past the end of the attribute.")
    object INSUFFICIENT_AUTHORIZATION :
        AttStatus(0x08, "The attribute requires authorization before it can be read or written.")

    object PREPARE_QUEUE_FULL : AttStatus(0x09, "Too many prepare writes have been queued.")
    object ATTRIBUTE_NOT_FOUND : AttStatus(0x0A, "No attribute found within the given attribute handle range.")
    object ATTRIBUTE_NOT_LONG : AttStatus(0x0B, "The attribute cannot be read using the Read Blob Request.")
    object ENCRYPTION_KEY_SIZE_TOO_SHORT :
        AttStatus(0x0C, "The Encryption Key Size used for encrypting this link is too short.")

    object INVALID_ATTRIBUTE_VALUE_LENGTH : AttStatus(0x0D, "The attribute value length is invalid for the operation.")
    object UNLIKELY_ERROR : AttStatus(
        0x0E,
        "The attribute request that was requested has encountered an error that was unlikely, and therefore could not be completed as requested."
    )

    object INSUFFICIENT_ENCRYPTION :
        AttStatus(0x0F, "The attribute requires encryption before it can be read or written.")

    object UNSUPPORTED_GROUP_TYPE : AttStatus(
        0x10,
        "The attribute type is not a supported grouping attribute as defined by a higher layer specification."
    )

    object INSUFFICIENT_RESOURCES : AttStatus(0x11, "Insufficient Resources to complete the request.")
    object DATABASE_OUT_OF_SYNC : AttStatus(0x12, "The server requests the client to rediscover the database.")
    object VALUE_NOT_ALLOWED : AttStatus(0x13, "The attribute parameter value was not allowed.")

    // ATT application errors: range is 0x80-0x9F

    /*
     * Android specific status
     * From the source code these errors can be found under the C Header file `gatt_api.h`.
     * https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/system/stack/include/gatt_api.h
     */
    object ANDROID_SUCCESS : AttStatus(0x00)
    object ANDROID_TOO_SHORT : AttStatus(0x7F) // never used in source code
    object ANDROID_NO_RESOURCES : AttStatus(0x80)
    object ANDROID_INTERNAL_ERROR : AttStatus(0x81)
    object ANDROID_WRONG_STATE : AttStatus(0x82)
    object ANDROID_DB_FULL : AttStatus(0x83) // never used in source code
    object ANDROID_BUSY : AttStatus(0x84)
    object ANDROID_ERROR : AttStatus(0x85)
    object ANDROID_CMD_STARTED : AttStatus(0x86)
    object ANDROID_ILLEGAL_PARAMETER : AttStatus(0x87)
    object ANDROID_PENDING : AttStatus(0x88)
    object ANDROID_AUTH_FAIL : AttStatus(0x89)
    object ANDROID_MORE : AttStatus(0x8A) // never used in source code
    object ANDROID_INVALID_CFG : AttStatus(0x8B) // never used in source code
    object ANDROID_SERVICE_STARTED : AttStatus(0x8C)
    object ANDROID_ENCRYPED_NO_MITM : AttStatus(0x8D)
    object ANDROID_NOT_ENCRYPTED : AttStatus(0x8E)
    object ANDROID_CONGESTED : AttStatus(0x8F)
    object ANDROID_DUP_REG : AttStatus(0x90)
    object ANDROID_ALREADY_OPEN : AttStatus(0x91)
    object ANDROID_CANCEL : AttStatus(0x92)

    // Other application errors, original range is 0x80-0x9F
    class ApplicationError(value: Int) : AttStatus(value)

    /*
     * Common Profile and Service Error Codes 0xE0–0xFF: Common profile and service error codes defined in Core
     * Specification Supplement, Part B, Common Profile and Service Error Codes v9.
     */
    object WRITE_REQUEST_REJECTED : AttStatus(
        0xFC,
        "A write operation cannot be fulfilled for reasons other than permissions in opposition to WRITE_NOT_PERMITTED."
    )

    object CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR_IMPROPERLY_CONFIGURED : AttStatus(
        0xFD,
        "The Client Characteristic Configuration descriptor is not configured according to the requirements of the profile or service."
    )

    object PROCEDURE_ALREADY_IN_PROGRESS : AttStatus(
        0xFE,
        "A profile or service request cannot be serviced because an operation that has been previously triggered is still in progress."
    )

    object OUT_OF_RANGE :
        AttStatus(0xFF, "An attribute value is out of range as defined by a profile or service specification.")

    class CommonProfileAndServiceError(value: Int) : AttStatus(value)

    class Other(value: Int) : AttStatus(value)

    companion object {
        fun valueOf(value: Int): AttStatus =
            when (value) {
                INVALID_HANDLE.value -> INVALID_HANDLE
                READ_NOT_PERMITTED.value -> READ_NOT_PERMITTED
                WRITE_NOT_PERMITTED.value -> WRITE_NOT_PERMITTED
                INVALID_PDU.value -> INVALID_PDU
                INSUFFICIENT_AUTHENTICATION.value -> INSUFFICIENT_AUTHENTICATION
                REQUEST_NOT_SUPPORTED.value -> REQUEST_NOT_SUPPORTED
                INVALID_OFFSET.value -> INVALID_OFFSET
                INSUFFICIENT_AUTHORIZATION.value -> INSUFFICIENT_AUTHORIZATION
                PREPARE_QUEUE_FULL.value -> PREPARE_QUEUE_FULL
                ATTRIBUTE_NOT_FOUND.value -> ATTRIBUTE_NOT_FOUND
                ATTRIBUTE_NOT_LONG.value -> ATTRIBUTE_NOT_LONG
                ENCRYPTION_KEY_SIZE_TOO_SHORT.value -> ENCRYPTION_KEY_SIZE_TOO_SHORT
                INVALID_ATTRIBUTE_VALUE_LENGTH.value -> INVALID_ATTRIBUTE_VALUE_LENGTH
                UNLIKELY_ERROR.value -> UNLIKELY_ERROR
                INSUFFICIENT_ENCRYPTION.value -> INSUFFICIENT_ENCRYPTION
                UNSUPPORTED_GROUP_TYPE.value -> UNSUPPORTED_GROUP_TYPE
                INSUFFICIENT_RESOURCES.value -> INSUFFICIENT_RESOURCES
                DATABASE_OUT_OF_SYNC.value -> DATABASE_OUT_OF_SYNC
                VALUE_NOT_ALLOWED.value -> VALUE_NOT_ALLOWED
                WRITE_REQUEST_REJECTED.value -> WRITE_REQUEST_REJECTED
                CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR_IMPROPERLY_CONFIGURED.value -> CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR_IMPROPERLY_CONFIGURED
                PROCEDURE_ALREADY_IN_PROGRESS.value -> PROCEDURE_ALREADY_IN_PROGRESS
                OUT_OF_RANGE.value -> OUT_OF_RANGE
                ANDROID_SUCCESS.value -> ANDROID_SUCCESS
                ANDROID_TOO_SHORT.value -> ANDROID_TOO_SHORT
                ANDROID_NO_RESOURCES.value -> ANDROID_NO_RESOURCES
                ANDROID_INTERNAL_ERROR.value -> ANDROID_INTERNAL_ERROR
                ANDROID_WRONG_STATE.value -> ANDROID_WRONG_STATE
                ANDROID_DB_FULL.value -> ANDROID_DB_FULL
                ANDROID_BUSY.value -> ANDROID_BUSY
                ANDROID_ERROR.value -> ANDROID_ERROR
                ANDROID_CMD_STARTED.value -> ANDROID_CMD_STARTED
                ANDROID_ILLEGAL_PARAMETER.value -> ANDROID_ILLEGAL_PARAMETER
                ANDROID_PENDING.value -> ANDROID_PENDING
                ANDROID_AUTH_FAIL.value -> ANDROID_AUTH_FAIL
                ANDROID_MORE.value -> ANDROID_MORE
                ANDROID_INVALID_CFG.value -> ANDROID_INVALID_CFG
                ANDROID_SERVICE_STARTED.value -> ANDROID_SERVICE_STARTED
                ANDROID_ENCRYPED_NO_MITM.value -> ANDROID_ENCRYPED_NO_MITM
                ANDROID_NOT_ENCRYPTED.value -> ANDROID_NOT_ENCRYPTED
                ANDROID_CONGESTED.value -> ANDROID_CONGESTED
                ANDROID_DUP_REG.value -> ANDROID_DUP_REG
                ANDROID_ALREADY_OPEN.value -> ANDROID_ALREADY_OPEN
                ANDROID_CANCEL.value -> ANDROID_CANCEL
                in 0x80..0x9F -> ApplicationError(value)
                in 0xE0..0xFF -> CommonProfileAndServiceError(value)
                else -> Other(value)
            }
    }
}

/*
 * Disconnection statuses codes are redefined from HCI codes in Android source code under the C Header file
 * `gatt_api.h`.
 * https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/system/stack/include/gatt_api.h
 */
sealed class ConnectionStatus(value: Int, override val details: String = "") : GattStatus(value) {
    override val prefix: String = "ConnectionStatus"

    /*
     * Android source code specific definition.
     */
    object CONN_OK : ConnectionStatus(HciStatus.SUCCESS.value)
    object CONN_L2C_FAILURE : ConnectionStatus(1, "General L2cap failure")
    object CONN_TIMEOUT : ConnectionStatus(HciStatus.CONNECTION_TIMEOUT.value, "connection timeout")
    object CONN_TERMINATE_PEER_USER :
        ConnectionStatus(HciStatus.REMOTE_USER_TERMINATED_CONNECTION.value, "connection terminate by peer user")

    object CONN_TERMINATE_LOCAL_HOST :
        ConnectionStatus(HciStatus.CONNECTION_TERMINATED_BY_LOCAL_HOST.value, "connection terminated by local host")

    object CONN_LMP_TIMEOUT :
        ConnectionStatus(HciStatus.LMP_OR_LL_RESPONSE_TIMEOUT.value, "connection fail for LMP response tout")

    object CONN_FAILED_ESTABLISHMENT :
        ConnectionStatus(HciStatus.CONNECTION_FAILED_TO_BE_ESTABLISHED_OR_SYNCHRONIZATION_TIMEOUT.value)

    object CONN_TERMINATED_POWER_OFF :
        ConnectionStatus(HciStatus.REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_POWER_OFF.value)

    object CONN_NONE : ConnectionStatus(257)

    class Other(value: Int) : ConnectionStatus(value)

    companion object {
        fun valueOf(value: Int): GattStatus =
            when (value) {
                CONN_FAILED_ESTABLISHMENT.value -> CONN_FAILED_ESTABLISHMENT
                CONN_L2C_FAILURE.value -> CONN_L2C_FAILURE
                CONN_LMP_TIMEOUT.value -> CONN_LMP_TIMEOUT
                CONN_NONE.value -> CONN_NONE
                CONN_OK.value -> CONN_OK
                CONN_TERMINATED_POWER_OFF.value -> CONN_TERMINATED_POWER_OFF
                CONN_TERMINATE_LOCAL_HOST.value -> CONN_TERMINATE_LOCAL_HOST
                CONN_TERMINATE_PEER_USER.value -> CONN_TERMINATE_PEER_USER
                CONN_TIMEOUT.value -> CONN_TIMEOUT
                else -> {
                    val hciStatus = HciStatus.valueOf(value)
                    val attStatus = AttStatus.valueOf(value)
                    when {
                        hciStatus !is HciStatus.Other -> hciStatus // Unlisted errors most likely occurred at HCI level
                        attStatus !is AttStatus.Other-> attStatus
                        else ->  Other(value)
                    }
                }
            }
    }
}

/**
 * List all HCI codes as defined from Bluetooth Core Specification 5.4,  Volume 1, Part F.
 * Also lists additional HCI codes as described in Android source code: file `hci_error_code.h`.
 * https://cs.android.com/android/platform/superproject/+/refs/heads/master:packages/modules/Bluetooth/system/stack/include/hci_error_code.h
 */
sealed class HciStatus(value: Int, override val details: String = "") : GattStatus(value) {
    override val prefix: String = "HciStatus"

    // HCI error codes from Bluetooth Core Specification 5.4,  Volume 1, Part F.
    object SUCCESS : HciStatus(0x00)
    object UNKNOWN_HCI_COMMAND : HciStatus(
        0x01,
        "The Controller does not understand the HCI Command Packet OpCode that the Host sent. The OpCode given might not correspond to any of the OpCodes specified in the Bluetooth Core Specifications, or any vendor-specific OpCodes, or the command may have not been implemented."
    )

    object UNKNOWN_CONNECTION_IDENTIFIER : HciStatus(
        0x02,
        "A command was sent from the Host that should identify a connection, but that connection does not exist or doesn ot identify the correct type of connection."
    )

    object HARDWARE_FAILURE : HciStatus(
        0x03,
        "Indicates to the Host that something in the Controller has failed in a manner that cannot be described with any other error code. The meaning implied with this error code is implementation dependent."
    )

    object PAGE_TIMEOUT : HciStatus(
        0x04,
        "A page timed out because of the Page Timeout configuration parameter. This error code may occur only with the 'HCI_Remote_Name_Request' and 'HCI_Create_Connection' commands"
    )

    object AUTHENTICATION_FAILURE : HciStatus(
        0x05,
        "Pairing or authentication failed due to incorrect results in the pairing or authentication procedure. This could be due to an incorrect PIN or Link Key."
    )

    object PIN_OR_KEY_MISSING :
        HciStatus(0x06, "Pairing failed because of a missing PIN, or authentication failed because of a missing Key.")

    object MEMORY_CAPACITY_EXCEEDED :
        HciStatus(0x07, "Indicates to the Host that the Controller has run out of memory to store new parameters.")

    object CONNECTION_TIMEOUT : HciStatus(
        0x08,
        "Either the link supervision timeout has expired for a given connection or that the synchronization timeout has expired for a given broadcast."
    )

    object CONNECTION_LIMIT_EXCEEDED : HciStatus(
        0x09,
        "An attempt to create another connection failed because the Controller is already at its limit of the number of connections it can support. The number of connections a device can support is implementation dependent."
    )

    object SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED : HciStatus(
        0x0A,
        "The Controller has reached the limit to the number of synchronous connections that can be achieved to a device. The number of synchronous connections a device can support is implementation dependent."
    )

    object CONNECTION_ALREADY_EXISTS : HciStatus(
        0x0B,
        "An attempt was made to create a new Connection to a device when there is already a connection to this device and multiple connections to the same device are not permitted."
    )

    object COMMAND_DISALLOWED : HciStatus(
        0x0C,
        "The command requested cannot be executed because the Controller is in a state where it cannot process this command at this time. This error shall not be used for command OpCodes where the error code Unknown HCI Command is valid."
    )

    object CONNECTION_REJECTED_DUE_TO_LIMITED_RESOURCES :
        HciStatus(0x0D, "A connection was rejected due to limited resources.")

    object CONNECTION_REJECTED_DUE_TO_SECURITY_REASONS : HciStatus(
        0x0E,
        "A connection was rejected due to security requirements not being fulfilled, like authentication or pairing."
    )

    object CONNECTION_REJECTED_DUE_TO_UNACCEPTABLE_BD_ADDR : HciStatus(
        0x0F,
        "A connection was rejected because this device does not accept the BD_ADDR. This may be because the device will only accept connections from specific BD_ADDRs."
    )

    object CONNECTION_ACCEPT_TIMEOUT_EXCEEDED :
        HciStatus(0x10, "The Connection Accept Timeout has been exceeded for this connection attempt.")

    object UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE : HciStatus(
        0x11,
        "A feature or parameter value in the HCI command is not supported. This error code shall not be used in an LMP PDU."
    )

    object INVALID_HCI_COMMAND_PARAMETERS : HciStatus(
        0x12,
        "At least one of the HCI command parameters is invalid.\nThis shall be used when:\n\t- the parameter total length is invalid.\n\t- a command parameter is an invalid type.\n\t- a connection identifier does not match the corresponding event.\n\t- a parameter is odd when it is required to be even.\n\t- a parameter is outside of the specified range.\n\t- two or more parameter values have inconsistent values.\nNote: An invalid type can be, for example, when a SCO connection_Handle is used where an ACL connection_Handle is required."
    )

    object REMOTE_USER_TERMINATED_CONNECTION : HciStatus(
        0x13,
        "The user on the remote device either terminated the connection or stopped broadcasting packets."
    )

    object REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_LOW_RESOURCES :
        HciStatus(0x14, "The remote device terminated the connection because of low resources.")

    object REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_POWER_OFF :
        HciStatus(0x15, "The remote device terminated the connection because the device is about to power off.")

    object CONNECTION_TERMINATED_BY_LOCAL_HOST : HciStatus(
        0x16,
        "Either the local device terminated the connection, terminated synchronization with a broadcaster or terminated broadcasting packets."
    )

    object REPEATED_ATTEMPTS : HciStatus(
        0x17,
        "The Controller is disallowing an authentication or pairing procedure because too little time has elapsed since the last authentication or pairing attempt failed."
    )

    object PAIRING_NOT_ALLOWED : HciStatus(
        0x18,
        "The device does not allow pairing. For example, when a device only allows pairing during a certain time window after some user input allows pairing."
    )

    object UNKNOWN_LMP_PDU : HciStatus(0x19, "The Controller has received an unknown LMP OpCode.")
    object UNSUPPORTED_REMOTE_FEATURE : HciStatus(
        0x1A,
        "The remote device does not support the feature associated with the issued command, LMP PDU or Link Layer Control PDU."
    )

    object SCO_OFFSET_REJECTED : HciStatus(0x1B, "The offset requested in the LMP_SCO_LINK_REQ PDU has been rejected.")
    object SCO_INTERVAL_REJECTED :
        HciStatus(0x1C, "The interval requested in the LMP_SCO_LINK_REQ PDU has been rejected.")

    object SCO_AIR_MODE_REJECTED :
        HciStatus(0x1D, "The air mode requested in the LMP_SCO_LINK_REQ PDU has been rejected.")

    object INVALID_LMP_OR_LL_PARAMETERS : HciStatus(
        0x1E,
        "Some LMP PDU / LL Control PDU parameters were invalid. This shall be used when:\n\t- the PDU length is invalid.\n\t- a parameter is odd when it is required to be even.\n\t- a parameter is outside of the specified range.\n\t- two or more parameters have inconsistent values."
    )

    object UNSPECIFIED_ERROR : HciStatus(0x1F, "No other error code specified is appropriate to use.")
    object UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE : HciStatus(
        0x20,
        "An LMP PDU or an LL Control PDU contains at least one parameter value that is not supported by the Controller at this time. This is normally used after a long negotiation procedure, for example during an LMP_HOLD_REQ, LMP_SNIFF_REQ and LMP_ENCRYPTION_KEY_SIZE_REQ PDU exchanges. This may be used by the Link Layer, for example during the Connection Parameters Request Link Layer Control procedure."
    )

    object ROLE_CHANGE_NOT_ALLOWED : HciStatus(0x21, "A Controller will not allow a role change at this time.")
    object LMP_OR_LL_RESPONSE_TIMEOUT : HciStatus(
        0x22,
        "An LMP transaction failed to respond within the LMP response timeout or an LL transaction failed to respond within the LL response timeout."
    )

    object LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION : HciStatus(
        0x23,
        "An LMP transaction or LL procedure has collided with the same transaction or procedure that is already in progress."
    )

    object LMP_PDU_NOT_ALLOWED : HciStatus(0x24, "A Controller sent an LMP PDU with an OpCode that was not allowed.")
    object ENCRYPTION_MODE_NOT_ACCEPTABLE :
        HciStatus(0x25, "The requested encryption mode is not acceptable at this time.")

    object LINK_KEY_CANNOT_BE_CHANGED :
        HciStatus(0x26, "A link key cannot be changed because a fixed unit key is being used.")

    object REQUESTED_QOS_NOT_SUPPORTED : HciStatus(0x27, "The requested Quality of Service is not supported.")
    object INSTANT_PASSED : HciStatus(
        0x28,
        "An LMP PDU or LL PDU that includes an instant cannot be performed because the instant when this would have occurred has passed."
    )

    object PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED :
        HciStatus(0x29, "It was not possible to pair as a unit key was requested and it is not supported.")

    object DIFFERENT_TRANSACTION_COLLISION :
        HciStatus(0x2A, "An LMP transaction or LL Procedure was started that collides with an ongoing transaction.")

    // 0x2B: reserved for future use
    object QOS_UNACCEPTABLE_PARAMETER : HciStatus(
        0x2C,
        "The specified quality of service parameters could not be accepted at this time, but other parameters may be acceptable."
    )

    object QOS_REJECTED : HciStatus(
        0x2D,
        "The specified quality of service parameters cannot be accepted and QoS negotiation should be terminated."
    )

    object CHANNEL_CLASSIFICATION_NOT_SUPPORTED :
        HciStatus(0x2E, "The Controller cannot perform channel assessment because it is not supported.")

    object INSUFFICIENT_SECURITY :
        HciStatus(0x2F, "The HCI command or LMP PDU sent is only possible on an encrypted link.")

    object PARAMETER_OUT_OF_MANDATORY_RANGE : HciStatus(
        0x30,
        "A parameter value requested is outside the mandatory range of parameters for the given HCI command or LMP PDU and the recipient does not accept that value."
    )

    // 0x31: reserved for future use
    object ROLE_SWITCH_PENDING : HciStatus(
        0x32,
        "A Role Switch is pending. This can be used when an HCI command or LMP PDU cannot be accepted because of a pending role switch. This can also be used to notify a peer device about a pending role switch."
    )

    object RESERVED_SLOT_VIOLATION : HciStatus(
        0x34,
        "The current Synchronous negotiation was terminated with the negotiation state set to Reserved Slot Violation."
    )

    object ROLE_SWITCH_FAILED : HciStatus(
        0x35,
        "A role switch was attempted but it failed and the original piconet structure is restored. The switch may have failed because the TDD switch or piconet switch failed."
    )

    object EXTENDED_INQUIRY_RESPONSE_TOO_LARGE : HciStatus(
        0x36,
        "The extended inquiry response, with the requested requirements for FEC, is too large to fit in any of the packet types supported by the Controller."
    )

    object SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST : HciStatus(
        0x37,
        "The IO capabilities request or response was rejected because the sending Host does not support Secure Simple Pairing even though the receiving Link Manager does."
    )

    object HOST_BUSY_PAIRING : HciStatus(
        0x38,
        "The Host is busy with another pairing operation and unable to support the requested pairing. The receiving device should retry pairing again later."
    )

    object CONNECTION_REJECTED_DUE_TO_NO_SUITABLE_CHANNEL_FOUND :
        HciStatus(0x39, "The Controller could not calculate an appropriate value for the Channel selection operation.")

    object CONTROLLER_BUSY :
        HciStatus(0x3A, "The operation was rejected because the Controller was busy and unable to process the request.")

    object UNACCEPTABLE_CONNECTION_PARAMETERS : HciStatus(
        0x3B,
        "The remote device either terminated the connection or rejected a request because of one or more unacceptable connection parameters."
    )

    object ADVERTISING_TIMEOUT : HciStatus(
        0x3C,
        "Advertising for a fixed duration completed or, for directed advertising, that advertising completed without a connection being created. (Formerly called Directed Advertising Timeout)"
    )

    object CONNECTION_TERMINATED_DUE_TO_MIC_FAILURE : HciStatus(
        0x3D,
        "Either the connection or the synchronization was terminated because the Message Integrity Check (MIC) failed on a received packet."
    )

    object CONNECTION_FAILED_TO_BE_ESTABLISHED_OR_SYNCHRONIZATION_TIMEOUT : HciStatus(
        0x3E,
        "The LL initiated a connection or initiated a synchronization to periodic advertising but the connection has failed to be established or the Link Layer failed to synchronize with the periodic advertising witihn 6 periodic advertising events of the first attempt."
    )

    // 0x3F previously used as MAC_CONNECTION_FAILED
    object COARSE_CLOCK_ADJUSTMENT_REJECTED_BUT_WILL_TRY_TO_ADJUST_USING_CLOCK_DRAGGING : HciStatus(
        0x40,
        "The Central, at this time, is unable to make a coarse adjustment to the piconet clock, using the supplied parameters. Instead the Central will attempt to move the clock using clock dragging."
    )

    object TYPE_0_SUBMAP_NOT_DEFINED :
        HciStatus(0x41, "The LMP PDU is rejected because the Type 0 submap is not currently defined.")

    object UNKNOWN_ADVERTISING_IDENTIFIER : HciStatus(
        0x42,
        "A command was sent from the Host that should identify an Advertising or Sync handle, but the Advertising or Sync handle does not exist."
    )

    object LIMIT_REACHED : HciStatus(
        0x43,
        "A number of operations requested has been reached and has indicated the completion of the activity (e.g., advertising or scanning)."
    )

    object OPERATION_CANCELLED_BY_HOST :
        HciStatus(0x44, "A request to the Controller issued by the Host and still pending was successfully canceled.")

    object PACKET_TOO_LONG : HciStatus(
        0x45,
        "An attempt was made to send or receive a packet that exceeds the maximum allowed packet length."
    )

    object TOO_LATE : HciStatus(0x46, "Information was provided too late to the Controller.")
    object TOO_EARLY : HciStatus(0x47, "Information was provided too early to the Controller.")

    /*
     * Android specific definition.
     * From the source code these errors can be found under the C Header file `hci_error_code.h`.
     * https://cs.android.com/android/platform/superproject/+/refs/heads/master:packages/modules/Bluetooth/system/stack/include/hci_error_code.h
     */
    object ANDROID_ERROR_UNDEFINED : HciStatus(0xFF)

    class Other(value: Int) : HciStatus(value)

    companion object {
        fun valueOf(value: Int): HciStatus =
            when (value) {
                ADVERTISING_TIMEOUT.value -> ADVERTISING_TIMEOUT
                ANDROID_ERROR_UNDEFINED.value -> ANDROID_ERROR_UNDEFINED
                AUTHENTICATION_FAILURE.value -> AUTHENTICATION_FAILURE
                CHANNEL_CLASSIFICATION_NOT_SUPPORTED.value -> CHANNEL_CLASSIFICATION_NOT_SUPPORTED
                COARSE_CLOCK_ADJUSTMENT_REJECTED_BUT_WILL_TRY_TO_ADJUST_USING_CLOCK_DRAGGING.value -> COARSE_CLOCK_ADJUSTMENT_REJECTED_BUT_WILL_TRY_TO_ADJUST_USING_CLOCK_DRAGGING
                COMMAND_DISALLOWED.value -> COMMAND_DISALLOWED
                CONNECTION_ACCEPT_TIMEOUT_EXCEEDED.value -> CONNECTION_ACCEPT_TIMEOUT_EXCEEDED
                CONNECTION_ALREADY_EXISTS.value -> CONNECTION_ALREADY_EXISTS
                CONNECTION_FAILED_TO_BE_ESTABLISHED_OR_SYNCHRONIZATION_TIMEOUT.value -> CONNECTION_FAILED_TO_BE_ESTABLISHED_OR_SYNCHRONIZATION_TIMEOUT
                CONNECTION_LIMIT_EXCEEDED.value -> CONNECTION_LIMIT_EXCEEDED
                CONNECTION_REJECTED_DUE_TO_LIMITED_RESOURCES.value -> CONNECTION_REJECTED_DUE_TO_LIMITED_RESOURCES
                CONNECTION_REJECTED_DUE_TO_NO_SUITABLE_CHANNEL_FOUND.value -> CONNECTION_REJECTED_DUE_TO_NO_SUITABLE_CHANNEL_FOUND
                CONNECTION_REJECTED_DUE_TO_SECURITY_REASONS.value -> CONNECTION_REJECTED_DUE_TO_SECURITY_REASONS
                CONNECTION_REJECTED_DUE_TO_UNACCEPTABLE_BD_ADDR.value -> CONNECTION_REJECTED_DUE_TO_UNACCEPTABLE_BD_ADDR
                CONNECTION_TERMINATED_BY_LOCAL_HOST.value -> CONNECTION_TERMINATED_BY_LOCAL_HOST
                CONNECTION_TERMINATED_DUE_TO_MIC_FAILURE.value -> CONNECTION_TERMINATED_DUE_TO_MIC_FAILURE
                CONNECTION_TIMEOUT.value -> CONNECTION_TIMEOUT
                CONTROLLER_BUSY.value -> CONTROLLER_BUSY
                DIFFERENT_TRANSACTION_COLLISION.value -> DIFFERENT_TRANSACTION_COLLISION
                ENCRYPTION_MODE_NOT_ACCEPTABLE.value -> ENCRYPTION_MODE_NOT_ACCEPTABLE
                EXTENDED_INQUIRY_RESPONSE_TOO_LARGE.value -> EXTENDED_INQUIRY_RESPONSE_TOO_LARGE
                HARDWARE_FAILURE.value -> HARDWARE_FAILURE
                HOST_BUSY_PAIRING.value -> HOST_BUSY_PAIRING
                INSTANT_PASSED.value -> INSTANT_PASSED
                INSUFFICIENT_SECURITY.value -> INSUFFICIENT_SECURITY
                INVALID_HCI_COMMAND_PARAMETERS.value -> INVALID_HCI_COMMAND_PARAMETERS
                INVALID_LMP_OR_LL_PARAMETERS.value -> INVALID_LMP_OR_LL_PARAMETERS
                LIMIT_REACHED.value -> LIMIT_REACHED
                LINK_KEY_CANNOT_BE_CHANGED.value -> LINK_KEY_CANNOT_BE_CHANGED
                LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION.value -> LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION
                LMP_OR_LL_RESPONSE_TIMEOUT.value -> LMP_OR_LL_RESPONSE_TIMEOUT
                LMP_PDU_NOT_ALLOWED.value -> LMP_PDU_NOT_ALLOWED
                MEMORY_CAPACITY_EXCEEDED.value -> MEMORY_CAPACITY_EXCEEDED
                OPERATION_CANCELLED_BY_HOST.value -> OPERATION_CANCELLED_BY_HOST
                PACKET_TOO_LONG.value -> PACKET_TOO_LONG
                PAGE_TIMEOUT.value -> PAGE_TIMEOUT
                PAIRING_NOT_ALLOWED.value -> PAIRING_NOT_ALLOWED
                PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED.value -> PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED
                PARAMETER_OUT_OF_MANDATORY_RANGE.value -> PARAMETER_OUT_OF_MANDATORY_RANGE
                PIN_OR_KEY_MISSING.value -> PIN_OR_KEY_MISSING
                QOS_REJECTED.value -> QOS_REJECTED
                QOS_UNACCEPTABLE_PARAMETER.value -> QOS_UNACCEPTABLE_PARAMETER
                REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_LOW_RESOURCES.value -> REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_LOW_RESOURCES
                REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_POWER_OFF.value -> REMOTE_DEVICE_TERMINATED_CONNECTION_DUE_TO_POWER_OFF
                REMOTE_USER_TERMINATED_CONNECTION.value -> REMOTE_USER_TERMINATED_CONNECTION
                REPEATED_ATTEMPTS.value -> REPEATED_ATTEMPTS
                REQUESTED_QOS_NOT_SUPPORTED.value -> REQUESTED_QOS_NOT_SUPPORTED
                RESERVED_SLOT_VIOLATION.value -> RESERVED_SLOT_VIOLATION
                ROLE_CHANGE_NOT_ALLOWED.value -> ROLE_CHANGE_NOT_ALLOWED
                ROLE_SWITCH_FAILED.value -> ROLE_SWITCH_FAILED
                ROLE_SWITCH_PENDING.value -> ROLE_SWITCH_PENDING
                SCO_AIR_MODE_REJECTED.value -> SCO_AIR_MODE_REJECTED
                SCO_INTERVAL_REJECTED.value -> SCO_INTERVAL_REJECTED
                SCO_OFFSET_REJECTED.value -> SCO_OFFSET_REJECTED
                SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST.value -> SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST
                SUCCESS.value -> SUCCESS
                SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED.value -> SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED
                TOO_EARLY.value -> TOO_EARLY
                TOO_LATE.value -> TOO_LATE
                TYPE_0_SUBMAP_NOT_DEFINED.value -> TYPE_0_SUBMAP_NOT_DEFINED
                UNACCEPTABLE_CONNECTION_PARAMETERS.value -> UNACCEPTABLE_CONNECTION_PARAMETERS
                UNKNOWN_ADVERTISING_IDENTIFIER.value -> UNKNOWN_ADVERTISING_IDENTIFIER
                UNKNOWN_CONNECTION_IDENTIFIER.value -> UNKNOWN_CONNECTION_IDENTIFIER
                UNKNOWN_HCI_COMMAND.value -> UNKNOWN_HCI_COMMAND
                UNKNOWN_LMP_PDU.value -> UNKNOWN_LMP_PDU
                UNSPECIFIED_ERROR.value -> UNSPECIFIED_ERROR
                UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE.value -> UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE
                UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE.value -> UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE
                UNSUPPORTED_REMOTE_FEATURE.value -> UNSUPPORTED_REMOTE_FEATURE
                else -> Other(value)
            }
    }
}
