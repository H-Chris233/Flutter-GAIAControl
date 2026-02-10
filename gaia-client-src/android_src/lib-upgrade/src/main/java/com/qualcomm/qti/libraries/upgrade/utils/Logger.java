/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.utils;

import android.util.Log;
import android.util.Pair;


public final class Logger {

    @SafeVarargs
    public static void log(boolean logged, String tag, String message,
                           Pair<String, Object>... pairs) {
        if (logged) {
            logDebug(tag, message, buildMessage(pairs));
        }
    }

    private static void logDebug(String tag, String method, String details) {
        Log.d(tag, method + ": " + details);
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
                .append("=")
                .append(pair.second == null ? "null" :
                                pair.second instanceof byte[] ?
                                        Utils.getHexadecimalStringFromBytes((byte[]) pair.second) :
                                        pair.second.toString());
    }

}
