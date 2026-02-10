/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
final class PublicationManagerImpl {

    private static final String TAG = "PublicationManagerImpl";

    private final Object mLock = new Object();

    private final ArrayMap<Subscription, Pair<List<Publisher>, List<Subscriber>>> mSubscriptions =
            new ArrayMap<>();

    PublicationManagerImpl() {
    }

    void subscribe(@NonNull Subscriber subscriber) {
        synchronized (mLock) {
            var lists = getLists(subscriber.getSubscription());
            addSubscriber(lists.first, lists.second, subscriber);
        }
    }

    void unsubscribe(@NonNull Subscriber subscriber) {
        synchronized (mLock) {
            if (!mSubscriptions.containsKey(subscriber.getSubscription())) {
                // unexpected
                Log.w(TAG, "[unsubscribe] called with unregistered subscription");
                return;
            }

            var lists = getLists(subscriber.getSubscription());
            removeSubscriber(lists.first, lists.second, subscriber);
        }
    }

    void release() {
        synchronized (mLock) {
            for (var pair : mSubscriptions.values()) {
                releasePublishers(pair.first);
                pair.second.clear();
            }
        }
    }

    void register(@NonNull Publisher publisher) {
        synchronized (mLock) {
            var lists = getLists(publisher.getSubscription());

            // add publishers
            var list = lists.first;
            if (list != null) {
                list.add(publisher);
            }

            // subscribe previous subscribers
            List<Subscriber> subscribers = lists.second;
            if (subscribers != null) {
                for (Subscriber subscriber : subscribers) {
                    //noinspection unchecked
                    publisher.subscribe(subscriber);
                }
            }
        }
    }

    void unregister(@NonNull Publisher publisher) {
        synchronized (mLock) {
            publisher.releaseSubscribers();

            if (!mSubscriptions.containsKey(publisher.getSubscription())) {
                // unexpected
                Log.w(TAG, "[unregister] called with unregistered subscription");
                return;
            }

            // get corresponding subscription list of publishers
            var list = getPublishers(publisher.getSubscription());
            if (list != null) {
                // remove publisher
                list.remove(publisher);
            }
        }
    }

    @NonNull
    private Pair<List<Publisher>, List<Subscriber>> getLists(@NonNull Subscription subscription) {
        Pair<List<Publisher>, List<Subscriber>> result = mSubscriptions.get(subscription);

        // subscription already exists, it is returned
        if (result != null) {
            return result;
        }

        // creating a new one
        return createSubscription(subscription);
    }

    private List<Publisher> getPublishers(@NonNull Subscription subscription) {
        var result = mSubscriptions.get(subscription);

        // subscription already exists, it is returned
        if (result == null) {
            result = createSubscription(subscription);
        }

        // returns the publishers from the pair
        return result.first;
    }

    private Pair<List<Publisher>, List<Subscriber>> createSubscription(Subscription subscription) {
        var result = new Pair<List<Publisher>, List<Subscriber>>(new ArrayList<>(), new ArrayList<>());
        mSubscriptions.put(subscription, result);
        return result;
    }

    private void releasePublishers(List<Publisher> list) {
        for (var publisher : list) {
            publisher.releaseSubscribers();
        }
        list.clear();
    }

    private void addSubscriber(List<Publisher> publishers, List<Subscriber> subscribers,
                               Subscriber subscriber) {
        if (publishers != null) {
            for (var publisher : publishers) {
                //noinspection unchecked
                publisher.subscribe(subscriber);
            }
        }

        if (subscribers != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    private void removeSubscriber(List<Publisher> publishers, List<Subscriber> subscribers,
                                  Subscriber subscriber) {
        if (publishers != null) {
            for (var publisher : publishers) {
                //noinspection unchecked
                publisher.unsubscribe(subscriber);
            }
        }

        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

}
