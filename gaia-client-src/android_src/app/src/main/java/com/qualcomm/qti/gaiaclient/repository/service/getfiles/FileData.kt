/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.getfiles

import android.os.Parcel
import android.os.Parcelable

data class FileData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdOn: String = "",
    val chipFamily: String = "",
    val tags: Array<String> = emptyArray(),
    val hardwareVersions: Array<String> = emptyArray()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        createdOn = parcel.readString() ?: "",
        chipFamily = parcel.readString() ?: ""
    ) {
        parcel.readStringArray(tags)
        parcel.readStringArray(hardwareVersions)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(createdOn)
        parcel.writeString(chipFamily)
        parcel.writeStringArray(tags)
        parcel.writeStringArray(hardwareVersions)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileData

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (createdOn != other.createdOn) return false
        if (chipFamily != other.chipFamily) return false
        if (!tags.contentEquals(other.tags)) return false
        if (!hardwareVersions.contentEquals(other.hardwareVersions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + createdOn.hashCode()
        result = 31 * result + chipFamily.hashCode()
        result = 31 * result + tags.contentHashCode()
        result = 31 * result + hardwareVersions.contentHashCode()
        return result
    }


    companion object CREATOR : Parcelable.Creator<FileData> {
        override fun createFromParcel(parcel: Parcel): FileData {
            return FileData(parcel)
        }

        override fun newArray(size: Int): Array<FileData?> {
            return arrayOfNulls(size)
        }
    }
}
