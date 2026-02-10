/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.lang.ref.WeakReference;

public class SendingData implements Comparable<SendingData> {

    private final long id;

    @NonNull
    private final byte[] mData;

    @NonNull
    private final WeakReference<SendListener> mListenerReference;

    private final boolean isFlushed;

    public SendingData(long id, @NonNull byte[] data, boolean flush, @Nullable SendListener listener) {
        this.id = id;
        this.mData = data;
        this.isFlushed = flush;
        this.mListenerReference = new WeakReference<>(listener);
    }

    boolean isFlushed() {
        return isFlushed;
    }

    byte[] getData() {
        return mData;
    }

    public long getId() {
        return id;
    }

    void onSending() {
        SendListener listener = mListenerReference.get();
        if (listener != null) {
            listener.onSending();
        }
    }

    void onSent() {
        SendListener listener = mListenerReference.get();
        if (listener != null) {
            listener.onSent();
        }
    }

    void onFailed() {
        SendListener listener = mListenerReference.get();
        if (listener != null) {
            listener.onFailed();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "SendingData{" +
                "id=" + id +
                ", flushed=" + isFlushed +
                ", listener=" + (mListenerReference.get() != null) +
                ", data=" + Utils.getHexadecimalStringFromBytes(mData) +
                '}';
    }

    @Override
    public int compareTo(SendingData that) {
        return Long.compare(this.id, that.id);
    }
}
