/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.version;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

/**
 * This is the version provided by the device at the handshake just after connecting to a device.
 * Those values can change at runtime if GAIA version is v3 and higher.
 * Refers to the GAIA V3 reference documents for more information.
 */
public class V2ApiVersion {

    private final int gaiaVersion;
    private final long protocolVersion;
    private final int apiVersionMajor;
    private final int apiVersionMinor;

    private static final int PROTOCOL_VERSION_OFFSET = 0;
    private static final int API_VERSION_MAJOR_OFFSET = 1;
    private static final int API_VERSION_MINOR_OFFSET = 2;

    public V2ApiVersion(byte[] payload) {
        this.protocolVersion = getUINT8(payload, PROTOCOL_VERSION_OFFSET); // cast to long
        this.apiVersionMajor = getUINT8(payload, API_VERSION_MAJOR_OFFSET);
        this.apiVersionMinor = getUINT8(payload, API_VERSION_MINOR_OFFSET);
        this.gaiaVersion = this.apiVersionMajor; // GAIA version is the api version major
    }

    public int getGaiaVersion() {
        return gaiaVersion;
    }

    public long getProtocolVersion() {
        return protocolVersion;
    }

    public int getApiVersionMajor() {
        return apiVersionMajor;
    }

    public int getApiVersionMinor() {
        return apiVersionMinor;
    }

    @NonNull
    @Override
    public String toString() {
        return "V2ApiVersion{" +
                "gaiaVersion=" + gaiaVersion +
                ", protocolVersion=" + protocolVersion +
                ", apiVersionMajor=" + apiVersionMajor +
                ", apiVersionMinor=" + apiVersionMinor +
                '}';
    }
}
