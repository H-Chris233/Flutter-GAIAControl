/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.files

import android.os.Parcel
import android.os.Parcelable
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.Filter
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesParameters

data class FilesFilters(
    val hardwareVersion: String = "",
    val withTags: Array<String> = emptyArray(),
    val withoutTags: Array<String> = emptyArray(),
    val createdAfter: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createStringArray() ?: emptyArray(),
        parcel.createStringArray() ?: emptyArray(),
        parcel.readString() ?: ""
    ) {
        parcel.readStringArray(withTags)
        parcel.readStringArray(withoutTags)
    }

    fun buildGetFilesParameters(applicationBuildId: String, applicationVersion: String): GetFilesParameters {
        return GetFilesParameters(
            applicationBuildId, applicationVersion, hardwareVersion,
            Filter(trueTags = withTags, falseTags = withoutTags, createdAfter = createdAfter)
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hardwareVersion)
        parcel.writeStringArray(withTags)
        parcel.writeStringArray(withoutTags)
        parcel.writeString(createdAfter)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilesFilters

        if (hardwareVersion != other.hardwareVersion) return false
        if (!withTags.contentEquals(other.withTags)) return false
        if (!withoutTags.contentEquals(other.withoutTags)) return false
        if (createdAfter != other.createdAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hardwareVersion.hashCode()
        result = 31 * result + withTags.contentHashCode()
        result = 31 * result + withoutTags.contentHashCode()
        result = 31 * result + createdAfter.hashCode()
        return result
    }

    override fun toString(): String {
        return "FilesFilters(hardwareVersion='$hardwareVersion', includedTags=${withTags.contentToString()}," +
                " excludedTags=${withoutTags.contentToString()}, createdAfter='$createdAfter')"
    }


    companion object CREATOR : Parcelable.Creator<FilesFilters> {
        override fun createFromParcel(parcel: Parcel): FilesFilters {
            return FilesFilters(parcel)
        }

        override fun newArray(size: Int): Array<FilesFilters?> {
            return arrayOfNulls(size)
        }
    }
}
