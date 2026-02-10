/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

public class UserFeatureFactory {

    public static UserFeatureSet buildUserFeature(FeatureType type, int size, byte[] content) {
        if (FeatureTypes.APPLICATION_FEATURE.equals(type.getType())) {
            return new ApplicationFeatureSet(type, size, content);
        }
        return new UserFeatureSet(type, size, content);
    }
}
