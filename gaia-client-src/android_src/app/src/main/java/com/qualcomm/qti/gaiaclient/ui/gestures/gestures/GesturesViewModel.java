/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.gestures;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestures;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.ImageViewData;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureLabelProvider;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureViewModel;
import com.qualcomm.qti.gaiaclient.utils.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GesturesViewModel extends GestureViewModel {

    private final StickyLiveData<List<GestureViewData>> gestures = new StickyLiveData<>();

    private final StickyLiveData<Event<Void>> resetToDefaultWarningLiveData = new StickyLiveData<>();

    @Inject
    public GesturesViewModel(@NonNull Application application,
                             FeaturesRepository featuresRepository,
                             GestureConfigurationRepository gestureConfigurationRepository) {
        super(application, featuresRepository, gestureConfigurationRepository);
    }

    @Override
    protected GestureConfigurationInfo[] getInfoToSupport() {
        return new GestureConfigurationInfo[]{GestureConfigurationInfo.SUPPORTED_GESTURES,
                                              GestureConfigurationInfo.GET_GESTURE_CONFIGURATION};
    }

    void observeGestures(@NonNull LifecycleOwner owner, @NonNull Observer<List<GestureViewData>> observer) {
        gestures.observe(owner, observer);
    }

    @NonNull
    LiveData<Event<Void>> getResetToDefaultWarningLiveData() {
        return resetToDefaultWarningLiveData;
    }

    void resetToDefaultRequest() {
        resetToDefaultWarningLiveData.postValue(new Event<>(null));
    }

    @Override
    protected void onConfiguration(Gesture gesture, Set<Configuration> update) {
        super.onConfiguration(gesture, update);
        addGesture(gesture, update);
    }

    private void addGesture(Gesture gesture, Set<Configuration> update) {
        // creating a new list and a new item for the update
        List<GestureViewData> list = getNewGestureList();
        GestureViewData data = getNewViewData(list, gesture);
        data.setSummary(update == null ? "" : GestureLabelProvider.getSummaryForGesture(getContext(), update));

        // Configurations don't always arrive in the correct order, so the list must be sorted.
        list.sort(Comparator.comparingInt(o -> o.getGesture().getId()));
        gestures.postValue(list);
    }

    private GestureViewData getNewViewData(List<GestureViewData> list, Gesture gesture) {
        Context context = getContext();
        List<GestureViewData> filtered = list.stream()
                .filter((viewData) -> viewData.getGesture().equals(gesture))
                .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            GestureViewData origin = filtered.get(0);
            GestureViewData newData = new GestureViewData(origin);
            list.remove(origin);
            list.add(newData); // order dos not matter here
            return newData;
        }
        else {
            String name = GestureLabelProvider.getGestureLabel(context, gesture);
            Integer imageId = gesture instanceof QTILGestures ? getImageResIdForGesture((QTILGestures) gesture) : null;
            ImageViewData image = imageId == null ? null : new ImageViewData(imageId, context.getString(
                    R.string.cont_desc_gesture, name));
            GestureViewData data = new GestureViewData(gesture, name, image);
            list.add(data);
            return data;
        }
    }

    @NonNull
    private List<GestureViewData> getNewGestureList() {
        List<GestureViewData> result = this.gestures.getValue();
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    private Integer getImageResIdForGesture(@NonNull QTILGestures gesture) {
        switch (gesture) {
            case TAP:
                return R.drawable.ic_tap;
            case SWIPE_UP:
                return R.drawable.ic_swipe_up;
            case SWIPE_DOWN:
                return R.drawable.ic_swipe_down;
            case TAP_AND_SWIPE_UP:
                return R.drawable.ic_tap_and_swipe_up;
            case TAP_AND_SWIP_DOWN:
                return R.drawable.ic_tap_and_swipe_down;
            case DOUBLE_TAP:
                return R.drawable.ic_double_tap;
            case LONG_PRESS:
                return R.drawable.ic_long_press;
            case EXTENDED_LONG_PRESS:
                return R.drawable.ic_ext_long_press;
            default:
                return null;
        }
    }
}
