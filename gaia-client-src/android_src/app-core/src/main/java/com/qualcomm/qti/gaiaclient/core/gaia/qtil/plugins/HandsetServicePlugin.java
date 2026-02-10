/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.data.HandsetServiceInfo;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.HandsetServicePublisher;

public interface HandsetServicePlugin {

    // to force the HandsetServicePlugin to implement HandsetServicePublisher
    // this is unused
    HandsetServicePublisher getHandsetServicePublisher();

    void setInfo(HandsetServiceInfo info, Object value);
}
