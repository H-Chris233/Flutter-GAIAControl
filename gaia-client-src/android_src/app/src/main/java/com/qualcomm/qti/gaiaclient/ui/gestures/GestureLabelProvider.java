/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILActions;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestureContexts;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GestureLabelProvider {

    public static String getGestureLabel(Context context, Gesture gesture) {
        if (gesture instanceof QTILGestures) {
            return getQTILGestureLabel(context, (QTILGestures) gesture);
        }
        else {
            int id = gesture == null ? -1 : gesture.getId();
            return context.getString(R.string.gesture_unknown, id);
        }
    }

    public static String getGestureContextLabel(Context appContext, GestureContext gestureContext) {
        if (gestureContext instanceof QTILGestureContexts) {
            return getQTILGestureContextLabel(appContext, (QTILGestureContexts) gestureContext);
        }
        else {
            int id = gestureContext == null ? -1 : gestureContext.getId();
            return appContext.getString(R.string.gesture_context_unknown, id);
        }
    }

    public static String getActionLabel(Context context, Action action) {
        if (action instanceof QTILActions) {
            return getQTILActionLabel(context, (QTILActions) action);
        }
        else {
            return context.getString(R.string.action_unknown, action.getId());
        }
    }

    public static String getSummaryForGesture(Context context, Set<Configuration> configurations) {
        List<Action> listed = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (Configuration config : configurations) {
            Action action = config.getAction();

            if (listed.isEmpty()) {
                listed.add(action);
                builder.append(getActionLabel(context, action));
            }
            else if (!listed.contains(action)) {
                listed.add(action);
                builder.append(", ").append(getActionLabel(context, action));
            }
        }

        return builder.toString();
    }

    private static String getQTILActionLabel(Context context, QTILActions action) {
        switch (action) {
            case MEDIA_PLAY_PAUSE_TOGGLE:
                return context.getString(R.string.action_media_play_pause_toggle);
            case MEDIA_STOP:
                return context.getString(R.string.action_media_stop);
            case MEDIA_NEXT_TRACK:
                return context.getString(R.string.action_media_next_track);
            case MEDIA_PREVIOUS_TRACK:
                return context.getString(R.string.action_media_previous_track);
            case MEDIA_SEEK_FORWARD:
                return context.getString(R.string.action_media_seek_forward);
            case MEDIA_SEEK_BACKWARD:
                return context.getString(R.string.action_media_seek_backward);
            case VOICE_ACCEPT_CALL:
                return context.getString(R.string.action_voice_accept_call);
            case VOICE_REJECT_CALL:
                return context.getString(R.string.action_voice_reject_call);
            case VOICE_HANG_UP_CALL:
                return context.getString(R.string.action_voice_hang_up_call);
            case VOICE_TRANSFER_CALL:
                return context.getString(R.string.action_voice_transfer_call);
            case VOICE_CALL_CYCLE:
                return context.getString(R.string.action_voice_call_cycle);
            case VOICE_JOIN_CALLS:
                return context.getString(R.string.action_voice_join_calls);
            case VOICE_MIC_MUTE_TOGGLE:
                return context.getString(R.string.action_voice_mic_mute_toggle);
            case GAMING_MODE_TOGGLE:
                return context.getString(R.string.action_gaming_mode_toggle);
            case ANC_ENABLE_TOGGLE:
                return context.getString(R.string.action_anc_enable_toggle);
            case ANC_NEXT_MODE:
                return context.getString(R.string.action_anc_next_mode);
            case VOLUME_UP:
                return context.getString(R.string.action_volume_up);
            case VOLUME_DOWN:
                return context.getString(R.string.action_volume_down);
            case RECONNECT_MRU_HANDSET:
                return context.getString(R.string.action_reconnect_mru_handset);
            case VA_PRIVACY_TOGGLE:
                return context.getString(R.string.action_va_privacy_toggle);
            case VA_FETCH_QUERY:
                return context.getString(R.string.action_va_fetch_query);
            case VA_PTT:
                return context.getString(R.string.action_va_ptt);
            case VA_CANCEL:
                return context.getString(R.string.action_va_cancel);
            case VA_FETCH:
                return context.getString(R.string.action_va_fetch);
            case VA_QUERY:
                return context.getString(R.string.action_va_query);
            case DISCONNECT_LRU_HANDSET:
                return context.getString(R.string.action_disconnect_lru_handset);
            case VOICE_JOIN_CALLS_HANG_UP:
                return context.getString(R.string.action_voice_join_calls_hang_up);
            default:
                return context.getString(R.string.action_unknown, action.getId());
        }
    }

    private static String getQTILGestureLabel(Context context, @NonNull QTILGestures gesture) {
        switch (gesture) {
            case TAP:
                return context.getString(R.string.gesture_tap);
            case SWIPE_UP:
                return context.getString(R.string.gesture_swipe_up);
            case SWIPE_DOWN:
                return context.getString(R.string.gesture_swipe_down);
            case TAP_AND_SWIPE_UP:
                return context.getString(R.string.gesture_tap_and_swipe_up);
            case TAP_AND_SWIP_DOWN:
                return context.getString(R.string.gesture_tap_and_swipe_down);
            case DOUBLE_TAP:
                return context.getString(R.string.gesture_double_tap);
            case LONG_PRESS:
                return context.getString(R.string.gesture_long_press);
            case EXTENDED_LONG_PRESS:
                return context.getString(R.string.gesture_extended_long_press);
            default:
                return context.getString(R.string.gesture_unknown, gesture.getId());
        }
    }

    private static String getQTILGestureContextLabel(Context appContext, @NonNull QTILGestureContexts gestureContext) {
        switch (gestureContext) {
            case PASSTHROUGH_GLOBAL:
                return appContext.getString(R.string.gesture_context_global);
            case MEDIA_PLAYER_STREAMING:
                return appContext.getString(R.string.gesture_context_media_streaming);
            case MEDIA_PLAYER_IDLE:
                return appContext.getString(R.string.gesture_context_media_idle);
            case VOICE_IN_CALL:
                return appContext.getString(R.string.gesture_context_in_call);
            case VOICE_INCOMING:
                return appContext.getString(R.string.gesture_context_incoming_call);
            case VOICE_OUTGOING:
                return appContext.getString(R.string.gesture_context_outgoing_call);
            case VOICE_IN_CALL_WITH_HELD:
                return appContext.getString(R.string.gesture_context_in_call_with_held);
            case HANDSET_DISCONNECTED:
                return appContext.getString(R.string.gesture_context_handset_disconnected);
            case HANDSET_CONNECTED:
                return appContext.getString(R.string.gesture_context_handset_connected);
            default:
                return appContext.getString(R.string.gesture_context_unknown, gestureContext.getId());
        }
    }

}
