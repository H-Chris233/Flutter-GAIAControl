/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.GestureConfigurationPublisher;

public interface GestureConfigurationPlugin {

    // to force the GestureConfigurationPlugin to implement GestureConfigurationPublisher
    // this is unused
    GestureConfigurationPublisher getGestureConfigurationPublisher();

    /**
     * @return <code>False</code> if the information is not supported by this method.
     */
    boolean fetchInfo(GestureConfigurationInfo info);

    /**
     * @return <code>False</code> if the information is not supported by this method.
     */
    boolean fetchInfo(GestureConfigurationInfo info, Object parameter);

    /**
     * @return <code>False</code> if the information is not supported by this method.
     */
    boolean setInfo(GestureConfigurationInfo info, Object value);

    void resetToDefault();
}
