/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.earbudfit;

public class EarbudFitViewData {

    private final String title;

    private final String message;

    private final EarbudFitState state;

    private final ResultsViewData results;

    public EarbudFitViewData(String title, String message, EarbudFitState state) {
        this.title = title;
        this.message = message;
        this.state = state;
        this.results = null;
    }

    public EarbudFitViewData(String title, String message, EarbudFitState state, ResultsViewData results) {
        this.title = title;
        this.message = message;
        this.state = state;
        this.results = results;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public EarbudFitState getState() {
        return state;
    }

    public ResultsViewData getResults() {
        return results;
    }
}
