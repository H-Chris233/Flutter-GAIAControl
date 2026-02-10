/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.deviceinfo;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ChargerStatus;

public class DeviceInformation {

    private EarbudPosition position = null;

    private String serialNumber = null;

    private String secondarySerialNumber = null;

    private String variantName = null;

    private ChargerStatus chargerStatus = null;

    public DeviceInformation() {
    }

    public EarbudPosition getPosition() {
        return position;
    }

    public String getSerialNumber() {
        return position == null ? serialNumber : null;
    }

    public String getSecondarySerialNumber() {
        return secondarySerialNumber;
    }

    public String getSerialNumberLeft() {
        return position == EarbudPosition.LEFT ? serialNumber : secondarySerialNumber;
    }

    public String getSerialNumberRight() {
        return position == EarbudPosition.RIGHT ? serialNumber : secondarySerialNumber;
    }

    public String getVariantName() {
        return variantName;
    }

    public ChargerStatus getChargerStatus() {
        return chargerStatus;
    }

    public void setPosition(EarbudPosition position) {
        this.position = position;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSecondarySerialNumber(String secondarySerialNumber) {
        this.secondarySerialNumber = secondarySerialNumber;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public void setChargerStatus(ChargerStatus status) {
        chargerStatus = status;
    }
}
