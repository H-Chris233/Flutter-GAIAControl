/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.utils;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.Collection;

public final class Logger {

    public static void log(boolean logged, String tag, String method) {
        if (logged) {
            logDebug(tag, method, "");
        }
    }

    public static void log(boolean logged, String tag, String method, String message) {
        if (logged) {
            Log.d(tag, "[" + method + "] " + message);
        }
    }

    @SafeVarargs
    public static void log(boolean logged, String tag, String method,
                           Pair<String, Object>... pairs) {
        if (logged) {
            logDebug(tag, method, buildMessage(pairs));
        }
    }

    @SafeVarargs
    public static void log(boolean logged, String tag, String method, String message,
                           Pair<String, Object>... pairs) {
        if (logged) {
            logDebug(tag, method, message + ": " + buildMessage(pairs));
        }
    }

    private static String buildMessage(Pair<String, Object>[] pairs) {
        StringBuilder message = new StringBuilder();
        if (pairs == null || pairs.length == 0) {
            return "";
        }

        // add first value
        appendKeyValue(message, pairs[0]);

        // add other values
        for (int i = 1; i < pairs.length; i++) {
            Pair<String, Object> pair = pairs[i];
            message.append(", ");
            appendKeyValue(message, pair);
        }

        return message.toString();
    }

    private static void appendKeyValue(StringBuilder builder, Pair<String, Object> pair) {
        builder.append(pair.first == null ? "key" : pair.first)
                .append("=");

        Object second = pair.second;

        if (second == null) {
            builder.append("null");
            return;
        }

        if (second instanceof byte[]) {
            builder.append(BytesUtils.getHexadecimalStringFromBytes((byte[]) second));
        }
        else if (second instanceof Collection) {
            appendCollection(builder, (Collection<?>) second);
        }
        else {
            builder.append(second.toString());
        }
    }

    private static void appendCollection(StringBuilder builder, Collection<?> collection) {
        if (collection == null) {
            builder.append("null");
            return;
        }

        builder.append("{");
        Object[] objects = collection.toArray();
        for (int i = 0; i < objects.length; i++) {
            if (0 < i) {
                builder.append(", ");
            }
            builder.append(objects[i]);
        }
        builder.append("}");
    }

    private static void logDebug(String tag, String method, String message) {
        Log.d(tag, "[" + method + "] " + message);
    }

}
