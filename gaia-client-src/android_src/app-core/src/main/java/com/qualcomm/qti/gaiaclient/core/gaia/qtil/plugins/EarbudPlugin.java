/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudPublisher;

public interface EarbudPlugin {

    void fetchEarbudInfo(EarbudInfo info);

    // to force the EarbudPlugin to implement EarbudPublisher
    // this is unused
    EarbudPublisher getEarbudPublisher();

}
