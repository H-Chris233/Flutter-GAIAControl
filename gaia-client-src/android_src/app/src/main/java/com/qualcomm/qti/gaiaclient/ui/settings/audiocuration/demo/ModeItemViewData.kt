/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData

class ModeItemViewData(val mode: ModeViewData, val isSelected: Boolean, val isEnabled: Boolean) : ListAdapterItemData {
    val value: String = mode.value.toString()
    val label: String = mode.label

    override fun isSameContent(itemData: ListAdapterItemData): Boolean {
        if (itemData !is ModeItemViewData) {
            return false
        }
        return value == itemData.value && label == itemData.label && isSelected == itemData.isSelected && isEnabled == itemData.isEnabled
    }

    override fun isSameItem(itemData: ListAdapterItemData): Boolean {
        return itemData is ModeItemViewData && mode == itemData.mode
    }

}
