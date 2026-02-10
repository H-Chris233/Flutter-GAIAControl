/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.discovery.viewdata;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData;

import java.util.Objects;

public class TitleViewData extends ItemViewData {

    private final String title;

    private final DiscoveryType type;

    public TitleViewData(DiscoveryType type, String title) {
        super(ItemType.TITLE);
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean isSameContent(ListAdapterItemData itemData) {
        if (itemData == null || getClass() != itemData.getClass()) {
            return false;
        }

        TitleViewData that = (TitleViewData) itemData;
        return Objects.equals(title, that.title) && type == that.type;
    }

    @Override
    public boolean isSameItem(ListAdapterItemData itemData) {
        if (this == itemData) {
            return true;
        }
        if (itemData == null || getClass() != itemData.getClass()) {
            return false;
        }
        TitleViewData that = (TitleViewData) itemData;
        return type == that.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TitleViewData that = (TitleViewData) o;
        return Objects.equals(title, that.title) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, type);
    }
}
