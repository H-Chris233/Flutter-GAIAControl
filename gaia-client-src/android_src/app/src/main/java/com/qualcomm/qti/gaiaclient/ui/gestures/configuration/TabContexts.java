/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestureContexts;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum TabContexts {

    MUSIC(R.string.gesture_tab_music,
          new LinkedHashSet<>(Arrays.asList(QTILGestureContexts.MEDIA_PLAYER_STREAMING,
                                            QTILGestureContexts.MEDIA_PLAYER_IDLE))),
    VOICE_CALLS(R.string.gesture_tab_voice_call,
                new LinkedHashSet<>(Arrays.asList(QTILGestureContexts.VOICE_IN_CALL,
                                                  QTILGestureContexts.VOICE_INCOMING,
                                                  QTILGestureContexts.VOICE_OUTGOING,
                                                  QTILGestureContexts.VOICE_IN_CALL_WITH_HELD
                ))),
    HANDSET(R.string.gesture_tab_handset, new LinkedHashSet<>(
            Arrays.asList(QTILGestureContexts.HANDSET_CONNECTED, QTILGestureContexts.HANDSET_DISCONNECTED))),
    GLOBAL(R.string.gesture_tab_global,
           new LinkedHashSet<>(Collections.singletonList(QTILGestureContexts.PASSTHROUGH_GLOBAL)));

    private final int labelID;

    private final Set<GestureContext> contexts;

    private static final TabContexts[] VALUES = values();

    TabContexts(int labelID, Set<GestureContext> contexts) {
        this.labelID = labelID;
        this.contexts = contexts;
    }

    public static int size() {
        return VALUES.length;
    }

    public static TabContexts valueOf(int ordinal) {
        for (TabContexts context : VALUES) {
            if (context.ordinal() == ordinal) {
                return context;
            }
        }

        return null;
    }

    public int getLabelID() {
        return labelID;
    }

    public Set<GestureContext> getContexts() {
        return contexts;
    }

    public String getLabel(Context context) {
        return context == null ? "" : context.getString(labelID);
    }
}
