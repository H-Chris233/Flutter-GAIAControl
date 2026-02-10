/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.gaia.GaiaManager;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType;
import com.qualcomm.qti.gaiaclient.core.requests.RequestManager;
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager;
import com.qualcomm.qti.gaiaclient.vendor.example.publications.MySubscriber;
import com.qualcomm.qti.gaiaclient.vendor.example.requests.MyRequest;
import com.qualcomm.qti.gaiaclient.vendor.example.vendors.MyV3Vendor;

/*
 * This class shows examples and recommendations on how the vendor extensions must interact with the core library.
 */
final class UseExample {

    // --------------------------------------------------------------------------
    // region VENDOR

    private MyV3Vendor myVendor;

    /*
     * The vendor needs to be registered with the GaiaManager to receive data from a connected
     * device.
     */
    void initMyV3Vendor() {
        // getting the manager
        GaiaManager gaiaManager = GaiaClientService.getGaiaManager();

        // the example V3 vendor requires the publication manager
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();

        // creating the vendor
        myVendor = new MyV3Vendor(gaiaManager.getSender(), publicationManager);

        // registering the vendor
        gaiaManager.registerVendor(myVendor);
    }

    /*
     * We recommend releasing all the vendors and their resources when they are not required
     * anymore, for instance when the application terminates.
     */
    void releaseVendor() {
        myVendor.stopAll();
        myVendor.removeAll();
    }

    /*
     * Note: using V1V2Vendor and raw Vendor is similar.
     */


    // endregion ----------------------------------------------------------------
    // region PUBLICATIONS & SUBSCRIPTIONS

    @SuppressWarnings("FieldCanBeLocal")
    private MySubscriber vendorSubscriber;

    /*
     * To be notified of events, a subscription needs to be registered with the PublicationManager.
     */
    void initSubscription() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();

        // initialisation
        vendorSubscriber = new MySubscriber() {

            @NonNull
            @Override
            public ExecutionType getExecutionType() {
                /*
                 * Only override this if the subscriber handles events for non UI purposes.
                 * As an example all the subscribers in the repositories of the UI module (app)
                 * do not override this as they are related to UI treatments.
                 * On another hand, all the subscribers used in the core library - for instance
                 * to know about the connection status - override this and return
                 * ExecutionType.BACKGROUND.
                 */
                return ExecutionType.UI_THREAD;
            }

            @Override
            public void somethingHappened() {
                Log.i("MY_TAG", "Something happened!");
            }

            @Override
            public void somethingElseHappened() {
                Log.i("MY_TAG", "Something else happened!");
            }
        };
        // subscribe to be notified of corresponding publications
        publicationManager.subscribe(vendorSubscriber);
    }

    /*
     * Note: subscribers and publishers are all released by the PublicationManager when the
     * application terminates. Implementations do not need to release them.
     */


    // endregion ----------------------------------------------------------------
    // region REQUEST

    /*
     * We recommend using the request system of the core library. It avoids blocking the
     * Main/UI Thread while executing the operation. Especially as Bluetooth connections are
     * managed over different threads.
     */
    void executeRequest(@NonNull Context context) {
        // creating the request
        MyRequest request = new MyRequest(myVendor, null);

        /*
         * Note:
         * to access vendors, our core library uses a QTILManager that it accesses using
         * GaiaClientService#getQtilManager
         */

        // executing the request
        RequestManager requestManager = GaiaClientService.getRequestManager();
        requestManager.execute(context, request);
    }


    // endregion ----------------------------------------------------------------
    // region BACKGROUND TASKS

    /*
     * We recommend using the TaskManager instead of running Handlers within vendors and plugins.
     * This is to avoid concurrency issues when creating handlers on a non UI Thread.
     * The TaskManager can be used to execute tasks on the UI thread or on a non UI thread. It can
     * also schedule tasks to run on a non UI Thread.
     * To schedule a task to run on the UI thread, use Handler#postDelayed and make sure that the
     * Handler is attached to the UI Thread.
     */
    void useTaskManager() {
        TaskManager taskManager = GaiaClientService.getTaskManager();

        // running a task in the UI thread
        taskManager.runOnMain(() -> { /* do something */});

        // running a task in background to relieve the UI thread
        taskManager.runInBackground(() -> { /* do something */});

        // equivalent of "Handler#postDelayed" is a scheduled task:
        int delay = 1000; // 1s
        taskManager.schedule(() -> { /* do something */}, delay);
    }

}
