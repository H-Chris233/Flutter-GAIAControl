/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.google.android.material.slider.LabelFormatter;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.CustomSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.ListSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SwitchSettingData;

import java.lang.ref.WeakReference;
import java.util.List;

public class AudioCurationDemoViewData extends SettingsViewData<AudioCurationDemoSettings> {

    AudioCurationDemoViewData(Context context) {
        super(context);
    }

    @Override
    public AudioCurationDemoSettings[] getKeys() {
        return AudioCurationDemoSettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, AudioCurationDemoSettings key) {
        switch (key) {
            case ANC_STATE:
            case ADAPTATION:
            case WIND_NOISE_DETECTION_STATE:
            case HOWLING_DETECTION_STATE:
            case ADVERSE_ACOUSTIC_STATE:
                // visible switch
                SettingData switchData = new SwitchSettingData();
                switchData.setVisible(true);
                return switchData;

            case MEDIA:
                SettingData mediaData = new ListSettingData();
                mediaData.setVisible(true);
                return mediaData;

            case MODE:
                SettingData modeData = new ModesSettingData();
                modeData.setVisible(true);
                return modeData;

            case FEED_FORWARD_GAIN:
            case FEEDBACK_GAIN:
                SettingData customData = new CustomSettingData();
                customData.setVisible(true);
                return customData;

            case LEAKTHROUGH_GAIN:
                SettingData customSettingData = new CustomSettingData();
                customSettingData.setVisible(false); // default is LEAKTHROUGH_GAIN_STEPPER
                return customSettingData;

            case LEAKTHROUGH_GAIN_STEPPER:
                DiscreteSliderSettingData stepperData = new DiscreteSliderSettingData();
                stepperData.setVisible(true); // default gain slider
                stepperData.setStepSize(1);
                stepperData.setMinValue(1);
                return stepperData;

            case LEFT_RIGHT_BALANCE:
                LeftRightBalanceSettingData balanceData = new LeftRightBalanceSettingData();
                balanceData.setVisible(true);
                final WeakReference<Context> appContextRef = new WeakReference<>(context.getApplicationContext());
                balanceData.setLabelFormatter(value -> {
                    Context appContext = appContextRef.get();
                    return appContext == null ? "" :
                           LabelProvider.getAlteredLabelForLeftRightBalance(appContext, (int) value);
                    // cast value to int as the values are integers only
                });
                return balanceData;

            case WIND_NOISE_REDUCTION:
            case ADVERSE_ACOUSTIC_GAIN_REDUCTION:
            case HOWLING_CONTROL_GAIN_REDUCTION:
                SettingData indicatorsData = new TouchpadIndicatorsSettingData();
                indicatorsData.setVisible(true);
                return indicatorsData;

            case NOISE_ID_CATEGORY:
                SettingData settingData = new SettingData();
                settingData.setVisible(true);
                settingData.setSummary(
                        context.getString(R.string.settings_audio_curation_demo_noise_id_category_empty));
                return settingData;

            default:
                // unexpected
                return new SettingData();
        }
    }

    public void observeGain(AudioCurationDemoSettings setting, LifecycleOwner owner,
                            Observer<GainPreferenceViewData> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof CustomSettingData) {
            ((CustomSettingData) data)
                    .observeData(owner, (update) -> observer.onChanged((GainPreferenceViewData) update));
        }
    }

    public void observeProgress(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                Observer<SliderViewData> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof CustomSettingData) {
            ((CustomSettingData) data).observeData(owner, (update) -> observer.onChanged((SliderViewData) update));
        }
    }

    public void observeModesList(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                 Observer<List<ModeViewData>> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof ModesSettingData) {
            ((ModesSettingData) data).observeModesList(owner, observer);
        }
    }

    public void observeMode(AudioCurationDemoSettings setting, LifecycleOwner owner, Observer<ModeViewData> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof ModesSettingData) {
            ((ModesSettingData) data).observeMode(owner, observer);
        }
    }

    public void observeDiscreteSliderMinValue(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                              Observer<Integer> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeMinValue(owner, observer);
        }
    }

    public void observeDiscreteSliderMaxValue(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                              Observer<Integer> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeMaxValue(owner, observer);
        }
    }

    public void observeDiscreteSliderValue(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                           Observer<Integer> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeValue(owner, observer);
        }
    }

    public void observeDiscreteSliderStepSize(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                              Observer<Integer> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeStepSize(owner, observer);
        }
    }

    public void observeDiscreteSliderLabel(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                           Observer<String> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeLabel(owner, observer);
        }
    }

    public void observeDiscreteSliderLabelFormatter(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                                    Observer<LabelFormatter> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof DiscreteSliderSettingData) {
            ((DiscreteSliderSettingData) data).observeLabelFormatter(owner, observer);
        }
    }

    public void observeLeftRightBalanceValue(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                             Observer<Integer> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof LeftRightBalanceSettingData) {
            ((LeftRightBalanceSettingData) data).observeValue(owner, observer);
        }
    }

    public void observeLeftRightBalanceLabel(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                             Observer<String> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof LeftRightBalanceSettingData) {
            ((LeftRightBalanceSettingData) data).observeLabel(owner, observer);
        }
    }

    public void observeLeftRightBalanceLabelFormatter(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                                      Observer<LabelFormatter> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof LeftRightBalanceSettingData) {
            ((LeftRightBalanceSettingData) data).observeLabelFormatter(owner, observer);
        }
    }

    public void observeWindNoiseReductionLeftTouchpadData(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                                          Observer<TouchpadViewData> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof TouchpadIndicatorsSettingData) {
            ((TouchpadIndicatorsSettingData) data).observeLeftTouchpadData(owner, observer);
        }
    }

    public void observeWindNoiseReductionRightTouchpadData(AudioCurationDemoSettings setting, LifecycleOwner owner,
                                                           Observer<TouchpadViewData> observer) {
        SettingData data = getSettingData(setting);
        if (data instanceof TouchpadIndicatorsSettingData) {
            ((TouchpadIndicatorsSettingData) data).observeRightTouchpadData(owner, observer);
        }
    }

    public void setGain(AudioCurationDemoSettings setting, GainPreferenceViewData update) {
        updateSetting(setting, (data) -> {
            if (data instanceof CustomSettingData) {
                ((CustomSettingData) data).setData(update);
            }
        });
    }

    public void setProgress(AudioCurationDemoSettings setting, SliderViewData update) {
        updateSetting(setting, (data) -> {
            if (data instanceof CustomSettingData) {
                ((CustomSettingData) data).setData(update);
            }
        });
    }

    public void setModesList(AudioCurationDemoSettings setting, List<ModeViewData> modes) {
        updateSetting(setting, (data) -> {
            if (data instanceof ModesSettingData) {
                ((ModesSettingData) data).setModes(modes);
            }
        });
    }

    public void setMode(AudioCurationDemoSettings setting, ModeViewData mode) {
        updateSetting(setting, (data) -> {
            if (data instanceof ModesSettingData) {
                ((ModesSettingData) data).setMode(mode);
            }
        });
    }

    public void setDiscreteSliderMaxValue(AudioCurationDemoSettings setting, int maxValue) {
        updateSetting(setting, (data) -> {
            if (data instanceof DiscreteSliderSettingData) {
                ((DiscreteSliderSettingData) data).setMaxValue(maxValue);
            }
        });
    }

    public void setDiscreteSliderValue(AudioCurationDemoSettings setting, int value) {
        updateSetting(setting, (data) -> {
            if (data instanceof DiscreteSliderSettingData) {
                ((DiscreteSliderSettingData) data).setValue(value);
            }
        });
    }

    public void setDiscreteSliderLabel(AudioCurationDemoSettings setting, String label) {
        updateSetting(setting, (data) -> {
            if (data instanceof DiscreteSliderSettingData) {
                ((DiscreteSliderSettingData) data).setLabel(label);
            }
        });
    }

    public void setDiscreteSliderLabelFormatter(AudioCurationDemoSettings setting, LabelFormatter formatter) {
        updateSetting(setting, (data) -> {
            if (data instanceof DiscreteSliderSettingData) {
                ((DiscreteSliderSettingData) data).setLabelFormatter(formatter);
            }
        });
    }

    public void setLeftRightBalanceValue(AudioCurationDemoSettings setting, int value) {
        updateSetting(setting, (data) -> {
            if (data instanceof LeftRightBalanceSettingData) {
                ((LeftRightBalanceSettingData) data).setValue(value);
            }
        });
    }

    public void setLeftRightBalanceLabel(AudioCurationDemoSettings setting, String label) {
        updateSetting(setting, (data) -> {
            if (data instanceof LeftRightBalanceSettingData) {
                ((LeftRightBalanceSettingData) data).setLabel(label);
            }
        });
    }

    public void setLeftTouchpadViewData(AudioCurationDemoSettings setting, TouchpadViewData touchpadViewData) {
        updateSetting(setting, (data) -> {
            if (data instanceof TouchpadIndicatorsSettingData) {
                ((TouchpadIndicatorsSettingData) data).setLeftTouchpadData(touchpadViewData);
            }
        });
    }

    public void setRightTouchpadViewData(AudioCurationDemoSettings setting, TouchpadViewData touchpadViewData) {
        updateSetting(setting, (data) -> {
            if (data instanceof TouchpadIndicatorsSettingData) {
                ((TouchpadIndicatorsSettingData) data).setRightTouchpadData(touchpadViewData);
            }
        });
    }
}
