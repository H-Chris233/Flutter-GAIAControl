/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.discovery.viewdata;

import androidx.annotation.IntDef;

import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData;

public abstract class ItemViewData implements ListAdapterItemData {

    @IntDef(flag = true, value = {ItemType.TITLE, ItemType.DEVICE})
    public @interface ItemType {

        int TITLE = 0;
        int DEVICE = 1;
    }

    @ItemType
    private final int type;

    public ItemViewData(@ItemType int type) {
        this.type = type;
    }

    @ItemType
    public int getType() {
        return type;
    }
}
