/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContextViewData implements ListAdapterItemData {

    @NonNull
    private final String label;

    @NonNull
    private final GestureContext context;

    private boolean isSwitchVisible = true;

    private boolean isSwitchEnabled = true;

    private boolean isSwitchChecked = true;

    @NonNull
    private List<ActionViewData> actions = Collections.emptyList();

    public ContextViewData(@NonNull GestureContext context, @NonNull String label) {
        this.label = label;
        this.context = context;
    }

    public ContextViewData(ContextViewData data) {
        this.label = data.label;
        this.context = data.context;
        this.isSwitchVisible = data.isSwitchVisible;
        this.isSwitchEnabled = data.isSwitchEnabled;
        this.isSwitchChecked = data.isSwitchChecked;

        this.actions = new ArrayList<>();
        data.actions.forEach(actionData -> actions.add(new ActionViewData(actionData)));
    }

    public void setSwitchVisible(boolean switchVisible) {
        isSwitchVisible = switchVisible;
        if (!switchVisible) {
            // not a left and right configuration
            setSwitchChecked(false);
        }
    }

    public void setSwitchEnabled(boolean switchEnabled) {
        isSwitchEnabled = switchEnabled;
    }

    public void setSwitchChecked(boolean checked) {
        isSwitchChecked = checked;
        getActionViewDataList().forEach(viewData -> viewData.showLeftAndRight(checked));
    }

    public void setActions(@NonNull List<ActionViewData> actions) {
        this.actions = actions;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @NonNull
    public GestureContext getContext() {
        return context;
    }

    @NonNull
    public List<ActionViewData> getActionViewDataList() {
        return actions;
    }

    public boolean isSwitchVisible() {
        return isSwitchVisible;
    }

    public boolean isSwitchEnabled() {
        return isSwitchEnabled;
    }

    public boolean isSwitchChecked() {
        return isSwitchChecked;
    }

    @Override
    public boolean isSameContent(ListAdapterItemData itemData) {
        if (this == itemData) {
            return true;
        }

        if (itemData == null || getClass() != itemData.getClass()) {
            return false;
        }

        ContextViewData that = (ContextViewData) itemData;

        return isSameItem(itemData)
                && isSwitchEnabled == that.isSwitchEnabled
                && isSwitchChecked == that.isSwitchChecked
                && compareListContents(actions, that.actions);
    }

    @Override
    public boolean isSameItem(ListAdapterItemData itemData) {
        return this.equals(itemData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextViewData that = (ContextViewData) o;
        return Objects.equals(label, that.label)
                && context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    private static boolean compareListContents(@NonNull List<ActionViewData> actionsThis,
                                               @NonNull List<ActionViewData> actionsThat) {
        if (actionsThat.size() != actionsThis.size()) {
            return false;
        }

        for (ActionViewData dataThis : actionsThis) {
            Stream<ActionViewData> streamThat = actionsThat.stream();
            if (streamThat.noneMatch(dataThis::isSameContent)) {
                return false;
            }
        }

        return true;
    }
}
