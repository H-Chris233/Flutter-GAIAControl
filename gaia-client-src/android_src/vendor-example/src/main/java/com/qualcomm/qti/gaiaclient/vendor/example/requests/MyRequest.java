/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.requests;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;
import com.qualcomm.qti.gaiaclient.vendor.example.vendors.MyV3Vendor;

/*
 * A simple example to implement a Request that will be executed using the RequestManager.
 */
public class MyRequest extends Request<Void, Void, Void> {

    private final MyV3Vendor vendor;

    /*
     * The constructor must call super(RequestListener<Result, Progress, Error> listener).
     * The constructor is used to initialise all objects that are used to execute the request.
     */
    public MyRequest(MyV3Vendor vendor,
                     RequestListener<Void, Void, Void> listener) {
        super(listener);
        this.vendor = vendor;

        /*
         * Note:
         * to access vendors, our QTIL core library uses a QTILManager that it accesses using
         * GaiaClientService#getQtilManager
         */
    }

    @Override
    public void run(@Nullable Context context) {
        /*
         * Implementation of the request.
         * The implementation should provide some feedback by calling onProgress(Progress
         * progress), onError(Error error) and onComplete(Result result).
         *
         * Our simple example only ends the execution by calling onComplete(Result result).
         */

        vendor.doSomething();

        /*
         * Once the request has completed it must call onComplete(Result result).
         */
        onComplete(null);
    }

    @Override
    protected void onEnd() {
        /*
         * This is called when the request has ended - either due to an error or completion.
         * Use this to release resources, for instance to unsubscribe a subscriber that was
         * registered either at the start or during the execution of the request.
         */
    }
}
