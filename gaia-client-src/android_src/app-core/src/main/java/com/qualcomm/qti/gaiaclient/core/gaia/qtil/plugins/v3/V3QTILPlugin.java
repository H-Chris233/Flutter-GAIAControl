/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.V3Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;

import androidx.annotation.NonNull;

public abstract class V3QTILPlugin extends V3Plugin {

    private static final int VENDOR_ID = 0x001D;

    private static final int DEFAULT_VERSION = 0x01;

    private final QTILFeature feature;

    private int version = DEFAULT_VERSION;

    V3QTILPlugin(QTILFeature feature, @NonNull GaiaSender sender) {
        super(VENDOR_ID, feature.getValue(), sender);
        this.feature = feature;
    }

    /**
     * @param version
     *         the version supported by the device.
     */
    public void start(int version) {
        this.version = version;
        start();
    }

    /**
     * <p>This is called when an error occurs from the GAIA process regarding this plugin.</p>
     * <p>This could be used for instance to notify a plugin that it was not possible to
     * register it for notifications.</p>
     *
     * @return True if this plugin is stopped as a result of the error. False is the plugin is
     * still running.
     */
    public boolean onError(V3QTILPluginError error) {
        if (error == V3QTILPluginError.NOTIFICATION_REGISTRATION_FAILED) {
            // default behaviour is to stop the plugin as notifications are required
            stop();
            return true;
        }
        return false;
    }

    public QTILFeature getQTILFeature() {
        return feature;
    }

    public int getVersion() {
        return version;
    }
}
