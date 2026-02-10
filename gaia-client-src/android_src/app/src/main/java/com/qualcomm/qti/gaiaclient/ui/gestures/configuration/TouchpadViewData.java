/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import java.util.Objects;

public class TouchpadViewData {

    private final TouchpadViewType type;

    private boolean isSelected;

    private boolean isVisible;

    public TouchpadViewData(TouchpadViewType type, boolean selected, boolean visible) {
        this.type = type;
        this.isSelected = selected;
        this.isVisible = visible;
    }

    public TouchpadViewData(TouchpadViewData origin) {
        this.type = origin.type;
        this.isSelected = origin.isSelected;
        this.isVisible = origin.isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TouchpadViewData that = (TouchpadViewData) o;
        return isSelected == that.isSelected && isVisible == that.isVisible && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isSelected, isVisible);
    }
}
