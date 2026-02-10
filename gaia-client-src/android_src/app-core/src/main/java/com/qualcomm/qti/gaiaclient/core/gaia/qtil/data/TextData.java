/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

public class TextData {

    private final String text;

    public TextData(byte[] data) {
        this.text = BytesUtils.getString(data);
    }

    public String getText() {
        return text;
    }
}
