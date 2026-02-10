/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestureContexts;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Touchpad;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfigurationType;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureLabelProvider;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureViewModel;
import com.qualcomm.qti.gaiaclient.utils.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ConfigurationListViewModel extends GestureViewModel {

    private static final Action ACTION_NOTHING = null;

    private final DeviceData deviceData = new DeviceData();

    private final FragmentParameters parameters = new FragmentParameters();

    private final StickyLiveData<List<ContextViewData>> contextViewDataListLiveData = new StickyLiveData<>();

    @NonNull
    private List<ContextViewData> contextViewDataList = Collections.emptyList();

    private final StickyLiveData<Event<Set<Configuration>>> showOverrideOtherDialogEventLiveData =
            new StickyLiveData<>();

    private final StickyLiveData<Event<Set<Configuration>>> showOverrideGlobalDialogEventLiveData =
            new StickyLiveData<>();

    @Inject
    public ConfigurationListViewModel(@NonNull Application application,
                                      FeaturesRepository featuresRepository,
                                      GestureConfigurationRepository gestureConfigurationRepository) {
        super(application, featuresRepository, gestureConfigurationRepository);
    }

    @Override
    protected GestureConfigurationInfo[] getInfoToSupport() {
        return new GestureConfigurationInfo[]{GestureConfigurationInfo.SUPPORTED_GESTURES,
                                              GestureConfigurationInfo.SUPPORTED_CONTEXTS,
                                              GestureConfigurationInfo.SUPPORTED_ACTIONS,
                                              GestureConfigurationInfo.GET_GESTURE_CONFIGURATION,
                                              GestureConfigurationInfo.RESET,
                                              GestureConfigurationInfo.TOUCHPAD_CONFIGURATION,
                                              GestureConfigurationInfo.SET_GESTURE_CONFIGURATION};
    }

    public void onSelection(GestureContext context, Action action, TouchpadViewType touchpadType) {
        Touchpad touchpad = getTouchpad(touchpadType);

        if (deviceData.configurations != null &&
                deviceData.configurations.hasConfiguration(context, action, touchpad)) {
            // configuration already exists
            return;
        }
        Set<Configuration> newConfigurations =
                DeviceConfigurations.Generator.generate(deviceData.configurations, context, touchpad, action);

        if (deviceData.configurations == null) {
            // unexpected: no configuration on the device
            setConfigurations(newConfigurations);
            return;
        }

        if (context.equals(QTILGestureContexts.PASSTHROUGH_GLOBAL)) {
            if (deviceData.configurations.hasNonGlobalConfigurations(touchpad) && !(action == null)) {
                // warn the user that the change will affect any existing configuration
                showOverrideOtherDialogEventLiveData.postValue(new Event<>(newConfigurations));
                // wait for the device to provide its new configuration to update the UI
            }
            else {
                // no conflict, setting up the new configurations
                setConfigurations(newConfigurations);
                // wait for the device to provide its new configuration to update the UI
            }
        }
        else {
            if (deviceData.configurations.hasGlobalConfigurations(touchpad)) {
                // wait for the device to provide its new configuration to update the UI
                showOverrideGlobalDialogEventLiveData.postValue(new Event<>(newConfigurations));
            }
            else {
                // no conflict, setting up the new configurations
                setConfigurations(newConfigurations);
                // wait for the device to provide its new configuration to update the UI
            }
        }
    }

    public void onSwitchChecked(GestureContext context, boolean checked) {
        ContextViewData contextViewData = getContextViewData(context);

        if (contextViewData == null) {
            return;
        }

        // update view data
        contextViewData.setSwitchChecked(checked);

        // update UI
        updateAndNotifyViewDataList(contextViewDataList);
    }

    public void overrideDialogAccepted(Set<Configuration> newConfigurations) {
        setConfigurations(newConfigurations);
    }

    @Override
    protected void onFeatureSupport(boolean supported) {
        super.onFeatureSupport(supported);
        if (!supported) {
            // feature cannot be used, this can happen when the device is disconnected.
            deviceData.reset();

            // update UI
            updateAndNotifyViewDataList(Collections.emptyList());
        }
    }

    @Override
    protected void onContexts(Set<GestureContext> update) {
        super.onContexts(update);

        if (update == null) {
            // If in "Gesture Configuration" screen and navigate to a gesture menu, the Fragment is blank.
            return;
        }

        // get supported contexts only
        Set<GestureContext> supportedContext =
                update.stream().filter(parameters.tabContext.getContexts()::contains).collect(Collectors.toSet());
        this.deviceData.supportedContexts = supportedContext;

        // generate view data for all contexts
        List<ContextViewData> contextsList = new ArrayList<>();
        supportedContext.forEach((context) -> contextsList
                .add(new ContextViewData(context, GestureLabelProvider.getGestureContextLabel(getContext(), context))));

        // update new view data with known data
        if (deviceData.supportedActions != null) {
            generateActionViewData(contextsList, deviceData.supportedActions);
        }
        updateViewData(contextsList, deviceData.touchpadConfiguration);
        updateViewData(contextsList);

        // update UI
        updateAndNotifyViewDataList(contextsList);
    }

    @Override
    protected void onActions(Set<Action> update) {
        super.onActions(update);

        deviceData.supportedActions = update;

        if (contextViewDataList.isEmpty()) {
            return;
        }

        generateActionViewData(contextViewDataList, update);

        // update view data
        updateViewData(contextViewDataList, deviceData.touchpadConfiguration);
        updateViewData(contextViewDataList);

        // update UI
        updateAndNotifyViewDataList(contextViewDataList);
    }

    @Override
    protected void onTouchpadConfiguration(TouchpadConfiguration touchpadConfiguration) {
        super.onTouchpadConfiguration(touchpadConfiguration);

        deviceData.touchpadConfiguration = touchpadConfiguration;

        if (contextViewDataList.isEmpty()) {
            return;
        }

        // update view data
        updateViewData(contextViewDataList, touchpadConfiguration);

        // update UI
        updateAndNotifyViewDataList(contextViewDataList);
    }

    @Override
    protected void onConfiguration(Gesture gesture, Set<Configuration> update) {
        super.onConfiguration(gesture, update);
        if (gesture == null || !gesture.equals(parameters.gesture)) {
            return;
        }

        deviceData.configurations = new DeviceConfigurations(update);

        if (contextViewDataList.isEmpty()) {
            return;
        }

        // update view data
        updateViewData(contextViewDataList);

        // update UI
        updateAndNotifyViewDataList(contextViewDataList);
    }

    void observeContextViewDataList(LifecycleOwner owner, @NonNull Observer<List<ContextViewData>> observer) {
        contextViewDataListLiveData.observe(owner, observer);
    }

    public LiveData<Event<Set<Configuration>>> getShowOverrideOtherDialogEvent() {
        return showOverrideOtherDialogEventLiveData;
    }

    public LiveData<Event<Set<Configuration>>> getShowOverrideGlobalDialogEvent() {
        return showOverrideGlobalDialogEventLiveData;
    }

    void setGesture(Gesture update) {
        this.parameters.gesture = update;
    }

    void setTabContext(TabContexts update) {
        this.parameters.tabContext = update;
    }

    private void generateActionViewData(@NonNull List<ContextViewData> contexts,
                                        @NonNull Set<Action> supportedByDevice) {
        contexts.forEach((contextViewData -> {
            List<ActionViewData> actionViewDataList = new ArrayList<>();
            Set<Action> supportedByGesture = this.parameters.gesture == null ?
                                             Collections.emptySet() : this.parameters.gesture.getSupportedActions();
            Set<Action> actions = filterActions(contextViewData, supportedByDevice, supportedByGesture);

            actions.forEach((action) -> actionViewDataList
                    .add(new ActionViewData(action, GestureLabelProvider
                            .getActionLabel(getContext(), action))));
            actionViewDataList.add(new ActionViewData(ACTION_NOTHING, getContext().getString(R.string.action_nothing)));
            contextViewData.setActions(actionViewDataList);
        }));
    }

    private Set<Action> filterActions(ContextViewData viewData, Set<Action> supportedByDevice,
                                      Set<Action> supportedByGesture) {
        GestureContext context = viewData == null ? null : viewData.getContext();
        Set<Action> supportedActions = context == null ? null : context.getSupportedActions();
        if (supportedActions == null) {
            return Collections.emptySet();
        }
        supportedByDevice = supportedByDevice == null ? Collections.emptySet() : supportedByDevice;
        supportedByGesture = supportedByGesture == null ? Collections.emptySet() : supportedByGesture;

        return supportedActions.stream().filter(supportedByDevice::contains)
                .filter(supportedByGesture::contains).collect(Collectors.toSet());
    }

    private void updateViewData(List<ContextViewData> contextsList, TouchpadConfiguration touchpadConfiguration) {
        if (contextsList == null || touchpadConfiguration == null) {
            // nothing to update
            return;
        }

        boolean isSwitchVisible = touchpadConfiguration.getType() != TouchpadConfigurationType.SINGLE_TOUCHPAD;
        contextsList.forEach(data -> {
            data.setSwitchVisible(isSwitchVisible);
            updateSwitchViewData(data,
                                 deviceData.configurations != null
                                         && deviceData.configurations.hasLeftOrRightConfiguration(data.getContext()));
        });
    }

    private void updateViewData(List<ContextViewData> contextsList) {
        if (contextsList == null || deviceData.configurations == null) {
            // nothing to update
            return;
        }

        contextsList.forEach(contextViewData -> {
            // update switch view data
            updateSwitchViewData(contextViewData,
                                 deviceData.configurations != null
                                         && deviceData.configurations
                                         .hasLeftOrRightConfiguration(contextViewData.getContext()));

            // update action view data -> get list of configurations for context
            Map<Touchpad, Action> actionMap =
                    deviceData.configurations == null ? null
                                                      : deviceData.configurations.get(contextViewData.getContext());
            actionMap = actionMap == null ? Collections.emptyMap() : actionMap;
            // update action view data -> add view data for configurations that uses an unrecognised action
            contextViewData
                    .setActions(addMissingActionViewData(contextViewData.getActionViewDataList(), actionMap));
            // update action view data
            updateActionsViewData(contextViewData, actionMap);
        });
    }

    private void updateSwitchViewData(ContextViewData data, boolean hasLeftOrRightConfiguration) {
        data.setSwitchEnabled(!hasLeftOrRightConfiguration);
        data.setSwitchChecked(data.isSwitchChecked() || hasLeftOrRightConfiguration);
    }

    private List<ActionViewData> addMissingActionViewData(List<ActionViewData> origin,
                                                          Map<Touchpad, Action> actionMap) {
        List<ActionViewData> viewDataList = new ArrayList<>(origin);

        Set<Action> actions = new LinkedHashSet<>(actionMap.values());
        actions.removeIf(action -> viewDataList.stream()
                .anyMatch(actionViewData -> action.equals(actionViewData.getAction())));

        actions.forEach((action) -> {
            int size = viewDataList.size();
            int index = size < 1 ? 0 : size - 1;
            viewDataList.add(index,
                             new ActionViewData(action, GestureLabelProvider.getActionLabel(getContext(), action)));
        });

        return viewDataList;
    }

    private void updateActionsViewData(ContextViewData contextViewData, final Map<Touchpad, Action> actionMap) {
        contextViewData.getActionViewDataList().forEach(actionViewData -> {
            Action action = actionViewData.getAction();
            actionViewData.showLeftAndRight(contextViewData.isSwitchChecked());
            for (TouchpadViewType type : TouchpadViewType.values()) {
                actionViewData.setTouchpadSelected(type, isTouchpadSelected(type, action, actionMap));
            }
        });
    }

    private boolean isTouchpadSelected(TouchpadViewType touchpadType, Action action, Map<Touchpad, Action> actionMap) {
        if (action == ACTION_NOTHING) {
            switch (touchpadType) {
                case LEFT:
                    return actionMap.get(Touchpad.LEFT) == null && actionMap.get(Touchpad.BOTH) == null;
                case RIGHT:
                    return actionMap.get(Touchpad.RIGHT) == null && actionMap.get(Touchpad.BOTH) == null;
                case TICK:
                default:
                    return actionMap.get(Touchpad.SINGLE) == null && actionMap.get(Touchpad.BOTH) == null;
            }
        }

        return action.equals(actionMap.get(Touchpad.BOTH))
                || (touchpadType == TouchpadViewType.LEFT && action.equals(actionMap.get(Touchpad.LEFT)))
                || (touchpadType == TouchpadViewType.RIGHT && action.equals(actionMap.get(Touchpad.RIGHT)))
                || (touchpadType == TouchpadViewType.TICK && action.equals(actionMap.get(Touchpad.SINGLE)));
    }

    private void updateAndNotifyViewDataList(@NonNull List<ContextViewData> update) {
        contextViewDataList = update;
        contextViewDataListLiveData.postValue(copyList(update));
    }

    private static List<ContextViewData> copyList(List<ContextViewData> update) {
        List<ContextViewData> copy = new ArrayList<>();
        update.forEach(data -> copy.add(new ContextViewData(data)));
        return copy;
    }

    @Nullable
    private ContextViewData getContextViewData(GestureContext context) {
        return contextViewDataList.stream().filter((contextViewData -> contextViewData.getContext().equals(context)))
                .findFirst().orElse(null);
    }

    private Touchpad getTouchpad(TouchpadViewType touchpadType) {
        switch (touchpadType) {
            case LEFT:
                return Touchpad.LEFT;
            case RIGHT:
                return Touchpad.RIGHT;
        }

        return deviceData.touchpadConfiguration.getType() == TouchpadConfigurationType.SINGLE_TOUCHPAD ?
               Touchpad.SINGLE : Touchpad.BOTH;
    }

    private void setConfigurations(Set<Configuration> configurations) {
        setConfigurations(parameters.gesture, configurations);
    }

    public static class DeviceData {
        Set<GestureContext> supportedContexts;
        Set<Action> supportedActions;
        TouchpadConfiguration touchpadConfiguration;
        DeviceConfigurations configurations;

        public void reset() {
            supportedContexts = null;
            supportedActions = null;
            touchpadConfiguration = null;
            configurations = null;
        }
    }

    private static class FragmentParameters {
        TabContexts tabContext;
        Gesture gesture;
    }

}
