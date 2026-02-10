/*
 * ************************************************************************************************
 * * © 2023-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.upgrade

import android.os.Parcel
import android.os.Parcelable

data class UpgradeOptions(
    val useDefault: Boolean,
    val expectedChunkSize: Int,
    val isLogged: Boolean,
    val isUploadFlushed: Boolean,
    val isUploadAcknowledged: Boolean,
    val useRwcp: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (useDefault) 1 else 0)
        parcel.writeInt(expectedChunkSize)
        parcel.writeByte(if (isLogged) 1 else 0)
        parcel.writeByte(if (isUploadFlushed) 1 else 0)
        parcel.writeByte(if (isUploadAcknowledged) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpgradeOptions> {
        override fun createFromParcel(parcel: Parcel): UpgradeOptions {
            return UpgradeOptions(parcel)
        }

        override fun newArray(size: Int): Array<UpgradeOptions?> {
            return arrayOfNulls(size)
        }
    }
}
