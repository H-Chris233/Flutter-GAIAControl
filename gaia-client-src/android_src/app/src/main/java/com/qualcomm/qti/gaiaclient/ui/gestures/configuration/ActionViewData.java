/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData;

import java.util.Objects;

public class ActionViewData implements ListAdapterItemData {

    private final Action action;
    @NonNull
    private final String label;
    @NonNull
    private final TouchpadViewData leftTouchpad;
    @NonNull
    private final TouchpadViewData rightTouchpad;
    @NonNull
    private final TouchpadViewData tickData;

    private boolean rowIsSelectable = false;

    public ActionViewData(Action action, @NonNull String label) {
        this(action, label,
             new TouchpadViewData(TouchpadViewType.LEFT, false, true),
             new TouchpadViewData(TouchpadViewType.RIGHT, false, true),
             new TouchpadViewData(TouchpadViewType.TICK, false, false));
    }

    public ActionViewData(ActionViewData data) {
        this.action = data.action;
        this.label = data.label;
        this.leftTouchpad = new TouchpadViewData(data.leftTouchpad);
        this.rightTouchpad = new TouchpadViewData(data.rightTouchpad);
        this.tickData = new TouchpadViewData(data.tickData);
        this.rowIsSelectable = data.rowIsSelectable;
    }

    private ActionViewData(Action action, @NonNull String label,
                           @NonNull TouchpadViewData leftTouchpad,
                           @NonNull TouchpadViewData rightTouchpad,
                           @NonNull TouchpadViewData tickData) {
        this.action = action;
        this.label = label;
        this.leftTouchpad = leftTouchpad;
        this.rightTouchpad = rightTouchpad;
        this.tickData = tickData;
    }

    public void showLeftAndRight(boolean show) {
        leftTouchpad.setVisible(show);
        rightTouchpad.setVisible(show);
        tickData.setVisible(!show);
        rowIsSelectable = !show;
    }

    public void setTouchpadSelected(TouchpadViewType type, boolean selected) {
        getTouchpadData(type).setSelected(selected);
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    public Action getAction() {
        return action;
    }

    @NonNull
    public TouchpadViewData getLeftTouchpad() {
        return leftTouchpad;
    }

    @NonNull
    public TouchpadViewData getRightTouchpad() {
        return rightTouchpad;
    }

    @NonNull
    public TouchpadViewData getTickData() {
        return tickData;
    }

    public boolean isRowSelectable() {
        return rowIsSelectable && !getTickData().isSelected(); // no need to be called if already selected
    }

    @Override
    public boolean isSameContent(ListAdapterItemData itemData) {
        if (this == itemData) {
            return true;
        }
        if (itemData == null || getClass() != itemData.getClass()) {
            return false;
        }

        ActionViewData that = (ActionViewData) itemData;

        return isSameItem(itemData)
                && Objects.equals(leftTouchpad, that.leftTouchpad)
                && Objects.equals(rightTouchpad, that.rightTouchpad)
                && Objects.equals(tickData, that.tickData);
    }

    @Override
    public boolean isSameItem(ListAdapterItemData itemData) {
        if (this == itemData) {
            return true;
        }
        if (itemData == null || getClass() != itemData.getClass()) {
            return false;
        }

        ActionViewData that = (ActionViewData) itemData;

        return Objects.equals(label, that.label)
                && Objects.equals(action, that.action);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ActionViewData && isSameItem((ActionViewData) o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, action);
    }

    private TouchpadViewData getTouchpadData(TouchpadViewType type) {
        switch (type) {
            case RIGHT:
                return rightTouchpad;
            case LEFT:
                return leftTouchpad;
            case TICK:
            default:
                return tickData;
        }
    }

}
