/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudFitPublisher;

public interface EarbudFitPlugin {

    // to force the EarbudFitPlugin to implement EarbudFitPublisher
    // this is unused
    EarbudFitPublisher getEarbudFitPublisher();

    void setFitTest(FitTestState state);
}
