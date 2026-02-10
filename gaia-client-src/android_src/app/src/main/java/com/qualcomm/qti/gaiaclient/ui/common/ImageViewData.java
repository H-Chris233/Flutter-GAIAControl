/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common;

import java.util.Objects;

public class ImageViewData {

    private final int resourceId;

    private final String contentDescription;

    public ImageViewData(int resourceId, String contentDescription) {
        this.resourceId = resourceId;
        this.contentDescription = contentDescription;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageViewData that = (ImageViewData) o;
        return resourceId == that.resourceId &&
                Objects.equals(contentDescription, that.contentDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, contentDescription);
    }
}
