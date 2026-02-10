/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.content.DialogInterface;

public class DialogData {

    private final String title;

    private final String message;

    private final String positiveLabel;

    private final DialogInterface.OnClickListener positiveListener;

    public DialogData(String title, String message, String positiveLabel,
                      DialogInterface.OnClickListener positiveListener) {
        this.title = title;
        this.message = message;
        this.positiveLabel = positiveLabel;
        this.positiveListener = positiveListener;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getPositiveLabel() {
        return positiveLabel;
    }

    public DialogInterface.OnClickListener getPositiveListener() {
        return positiveListener;
    }
}
