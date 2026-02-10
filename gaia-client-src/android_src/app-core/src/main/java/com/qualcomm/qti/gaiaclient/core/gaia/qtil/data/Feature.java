/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import java.util.Objects;

public class Feature {

    private final int featureId;
    private final int version;

    public Feature(int featureId, int version) {
        this.featureId = featureId;
        this.version = version;
    }

    public int getFeatureId() {
        return featureId;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feature feature = (Feature) o;
        return featureId == feature.featureId &&
                version == feature.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureId, version);
    }
}
