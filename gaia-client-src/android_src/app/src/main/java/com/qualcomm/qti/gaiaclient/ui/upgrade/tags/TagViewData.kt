/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.tags

import android.content.Context

data class TagViewData(val label: String, val backgroundColor: Int, val textColor: Int) {
    constructor(context: Context, tag: Tag) : this(
        tag.id,
        context.getColor(tag.backgroundColor),
        context.getColor(tag.textColor)
    )
}
