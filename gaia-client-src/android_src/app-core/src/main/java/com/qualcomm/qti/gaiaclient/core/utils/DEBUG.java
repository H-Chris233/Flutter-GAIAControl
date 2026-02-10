/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.utils;

public final class DEBUG {

    public static class Bluetooth {

        public static class Rfcomm {
            public static final boolean COMMUNICATION_THREAD = false;
            public static final boolean CONNECTION_THREAD = false;
            public static final boolean UUID_FETCHER = false;
            public static final boolean UUID_RECEIVER = false;
        }

        public static class Gatt {
            public static final boolean HANDLER = false;
            public static final boolean OPERATIONS_MANAGER = false;
            public static final boolean CONNECTION = false;
        }

        public static final boolean DATA = false;
        public static final boolean CLIENT = false;
        public static final boolean TRANSPORT_MANAGER = false;
        public static final boolean RECONNECTION_OBSERVER = false;
        public static final boolean RECONNECTION_DELEGATE = false;
        public static final boolean DISCOVERY_RECEIVER = false;
        public static final boolean RWCP = false;
    }

    public static class Gaia {

        public static final boolean PLUGIN = false;
        public static final boolean GAIA_PROTOCOL_CLIENT = false;
        public static final boolean GAIA_STREAM_ANALYSER = false;
        public static final boolean V1V2_VENDOR = false;
        public static final boolean V3_VENDOR = false;
        public static final boolean VENDOR_HANDLER = false;
    }

    public static class QTIL {

        public static final boolean V3_ANC_PLUGIN = false;
        public static final boolean V3_AUDIO_CURATION_PLUGIN = false;
        public static final boolean V3_EARBUD_FIT_PLUGIN = false;
        public static final boolean V3_HANDSET_SERVICE_PLUGIN = false;
        public static final boolean V3_VOICE_PROCESSING_PLUGIN = false;
        public static final boolean V3_GESTURE_CONFIGURATION_PLUGIN = false;
        public static final boolean V3_BATTERY_PLUGIN = false;
        public static final boolean V3_XPAN_PLUGIN = false;
        public static final boolean QTIL_VENDOR = false;
        public static final boolean QTIL_V3_VENDOR = false;
    }

    public static class Request {

        public static final boolean CONNECTION_REQUEST = false;
        public static final boolean REQUEST = false;
    }

    public static class Logs {

        public static final boolean PLUGIN_HELPER = false;
    }

    public static class Upgrade {

        public static final boolean PLUGIN_HELPER = false;
        public static final boolean PLUGIN_HELPER_DETAILS = false;
        public static final boolean UPGRADE_MANAGER = false;
    }

}
