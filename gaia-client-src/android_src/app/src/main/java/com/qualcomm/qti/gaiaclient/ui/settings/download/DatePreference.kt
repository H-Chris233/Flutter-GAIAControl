/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.download

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.utils.DATE_ISO_8601_PATTERN
import com.qualcomm.qti.gaiaclient.utils.format
import java.util.Date

class DatePreference : Preference {

    var dateInMs: Long = NO_SELECTION
        set(value) {
            field = value
            persistLong(value)
            summary =
                if (value == NO_SELECTION) context.getString(R.string.settings_download_created_after_no_selection)
                else Date(value).format("EEE, d MMM yyyy")
        }

    val dateIso8601: String
        get() = if (dateInMs == NO_SELECTION) "" else Date(dateInMs).format(DATE_ISO_8601_PATTERN) ?: ""

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context!!, attrs, defStyleAttr, defStyleRes)

    override fun setPersistent(persistent: Boolean) {
        super.setPersistent(persistent)
        if (persistent) {
            try {
                dateInMs = getPersistedLong(NO_SELECTION)
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        const val NO_SELECTION: Long = -1
        const val DATE_PICKER_TAG: String = "DATE_PICKER"
    }
}
