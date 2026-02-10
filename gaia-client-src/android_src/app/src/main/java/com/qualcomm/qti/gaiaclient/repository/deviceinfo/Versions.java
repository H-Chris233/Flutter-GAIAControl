/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.deviceinfo;

public class Versions {

    private Integer gaia = null;

    private Long protocol = null;

    private String application = null;

    public Versions() {
    }

    public Integer getGaia() {
        return gaia;
    }

    public void setGaia(Integer gaia) {
        this.gaia = gaia;
    }

    public Long getProtocol() {
        return protocol;
    }

    public void setProtocol(Long protocol) {
        this.protocol = protocol;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
