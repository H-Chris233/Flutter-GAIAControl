/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings;

public abstract class SettingsViewData<K extends Settings> {

    private final ArrayMap<K, StickyLiveData<SettingData>> mSettings;

    public SettingsViewData(Context context) {
        mSettings = buildSettings(context);
    }

    public abstract K[] getKeys();

    protected abstract SettingData initSettingData(Context context, K key);

    public void observeVisibility(K key, LifecycleOwner owner, Observer<Boolean> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data != null) {
            data.observeVisibility(owner, observer);
        }
    }

    public void observeEnable(K key, LifecycleOwner owner, Observer<Boolean> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data != null) {
            data.observeEnable(owner, observer);
        }
    }

    public void observeSummary(K key, LifecycleOwner owner, Observer<String> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data != null) {
            data.observeSummary(owner, observer);
        }
    }

    public void observeValue(K key, LifecycleOwner owner, Observer<String> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data instanceof EditTextSettingData) {
            ((EditTextSettingData) data).observeValue(owner, observer);
        }
        if (data instanceof ListSettingData) {
            ((ListSettingData) data).observeValue(owner, observer);
        }
    }

    public void observeEntries(K key, LifecycleOwner owner, Observer<String[]> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data instanceof ListSettingData) {
            ((ListSettingData) data).observeEntries(owner, observer);
        }
    }

    public void observeEntryValues(K key, LifecycleOwner owner, Observer<String[]> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data instanceof ListSettingData) {
            ((ListSettingData) data).observeEntryValues(owner, observer);
        }
    }

    public void observeChecked(K key, LifecycleOwner owner, Observer<Boolean> observer) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data instanceof SwitchSettingData) {
            ((SwitchSettingData) data).observeChecked(owner, observer);
        }
    }

    public void setSettingVisible(K key, boolean visible) {
        updateSetting(key, (data) -> data.setVisible(visible));
    }

    public void setSettingEnable(K key, boolean enabled) {
        updateSetting(key, (data) -> data.setEnabled(enabled));
    }

    public void setEnable(boolean enabled) {
        for (K key : getKeys()) {
            setSettingEnable(key, enabled);
        }
    }

    public void setSettingSummary(K key, String summary) {
        updateSetting(key, (data) -> data.setSummary(summary));
    }

    public void setSettingEntries(K key, Pair<String[], String[]> entries) {
        updateSetting(key, (data) -> {
            if (data instanceof ListSettingData) {
                ((ListSettingData) data).setEntries(entries);
            }
        });
    }

    public void setSettingValue(K key, String value) {
        updateSetting(key, (data) -> {
            if (data instanceof ListSettingData) {
                ((ListSettingData) data).setValue(value);
            }
            else if (data instanceof EditTextSettingData) {
                ((EditTextSettingData) data).setValue(value);
            }
        });
    }

    public String getSettingValue(K key) {
        SettingData data = getSettingData(key);
        if (data instanceof ListSettingData) {
            return ((ListSettingData) data).getValue();
        }
        else if (data instanceof EditTextSettingData) {
            return ((EditTextSettingData) data).getValue();
        }
        return null;
    }

    public void setSettingSwitch(K key, boolean isChecked) {
        updateSetting(key, (data) -> {
            if (data instanceof SwitchSettingData) {
                ((SwitchSettingData) data).setChecked(isChecked);
            }
        });
    }

    @Nullable
    protected SettingData getSettingData(K key) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        return liveData == null ? null : liveData.getValue();
    }

    protected void updateSetting(K key, @NonNull Updater updater) {
        StickyLiveData<SettingData> liveData = mSettings.get(key);
        SettingData data = liveData == null ? null : liveData.getValue();
        if (data != null) {
            updater.update(data);
            liveData.postValue(data);
        }
    }

    private ArrayMap<K, StickyLiveData<SettingData>> buildSettings(Context context) {
        ArrayMap<K, StickyLiveData<SettingData>> settings = new ArrayMap<>();
        for (K key : getKeys()) {
            settings.put(key, new StickyLiveData<>(initSettingData(context, key)));
        }
        return settings;
    }

    protected interface Updater {
        void update(SettingData data);
    }
}
