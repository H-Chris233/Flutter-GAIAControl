/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.os.Parcel
import android.os.Parcelable
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Device
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters

data class FeedbackForm(
    val title: String, val description: String, val reporter: String?, val hardwareVersion: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        reporter = parcel.readString(),
        hardwareVersion = parcel.readString()
    )

    fun buildParameters(applicationVersion: String, applicationBuildId: String): FeedbackParameters {
        return FeedbackParameters(
            title = title, description = description, reporter = reporter, device = Device(
                applicationVersion = applicationVersion,
                applicationBuildId = applicationBuildId,
                hardwareVersion = hardwareVersion ?: ""
            )
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(reporter)
        parcel.writeString(hardwareVersion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeedbackForm> {
        override fun createFromParcel(parcel: Parcel): FeedbackForm {
            return FeedbackForm(parcel)
        }

        override fun newArray(size: Int): Array<FeedbackForm?> {
            return arrayOfNulls(size)
        }
    }
}
