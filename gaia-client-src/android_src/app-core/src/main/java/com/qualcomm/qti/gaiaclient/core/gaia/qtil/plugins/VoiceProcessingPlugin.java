/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.VoiceProcessingPublisher;

public interface VoiceProcessingPlugin {

    // to force the VoiceProcessingPlugin to implement VoiceProcessingPublisher
    // this is unused
    VoiceProcessingPublisher getVoiceProcessingPublisher();

    void getSupportedEnhancements();

    void getConfiguration(Capability capability);

    void setConfiguration(VoiceEnhancementConfiguration configuration);
}
