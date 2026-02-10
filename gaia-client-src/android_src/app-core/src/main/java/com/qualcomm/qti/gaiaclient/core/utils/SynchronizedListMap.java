/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.utils;

import java.util.LinkedList;
import java.util.function.Consumer;

import androidx.collection.ArrayMap;

/**
 * This map manages a list of V elements for K keys.
 * It is thread safe for all actions.
 */
public class SynchronizedListMap<K, V> {

    private final Object mLock = new Object();

    private final ArrayMap<K, LinkedList<V>> mMap = new ArrayMap<>();

    public void put(K key, V value) {
        synchronized (mLock) {
            LinkedList<V> list = mMap.get(key);

            if (list == null) {
                list = new LinkedList<>();
                mMap.put(key, list);
            }

            list.add(value);
        }
    }


    public V poll(K key) {
        V value = null;

        synchronized (mLock) {
            LinkedList<V> list = mMap.get(key);

            if (list != null) {
                value = list.poll();
                if (list.isEmpty()) {
                    mMap.remove(key);
                }
            }
        }

        return value;
    }

    public void remove(K key, V value) {
        synchronized (mLock) {
            LinkedList<V> list = mMap.get(key);

            if (list != null) {
                list.remove(value);
                if (list.isEmpty()) {
                    mMap.remove(key);
                }
            }
        }
    }

    /**
     * Applies the action on all the V elements contained in the map prior to clear the map.
     */
    public void clear(Consumer<V> action) {
        synchronized (mLock) {
            if (action != null) {
                mMap.forEach((k, v) -> v.forEach(action));
            }
            mMap.clear();
        }
    }

}
