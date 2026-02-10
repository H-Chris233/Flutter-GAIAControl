/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

import java.util.Objects;

public class BandInfo {

    public static final int BAND_LENGTH = 7;
    private static final int FREQUENCY_OFFSET = 0;
    private static final int Q_OFFSET = 2;
    private static final int FILTER_OFFSET = 4;
    private static final int GAIN_OFFSET = 5;

    private static final double GAIN_FACTOR = 60.0;
    private static final double Q_FACTOR = 4096.0;

    private final int id;

    private final int frequency;
    private final double q;
    private final Filter filter;
    private final double gain;

    /**
     * <p>The configuration of a band is represented as follows within an array of bytes:
     * <blockquote><pre>
     * 0 bytes     1           2           3           4             5           6           7
     * +-----------+-----------+-----------+-----------+-------------+-----------+-----------+
     * |   CENTRE FREQUENCY    |           Q           | FILTER TYPE |          GAIN         |
     * +-----------+-----------+-----------+-----------+-------------+-----------+-----------+
     * </pre> </blockquote></p>
     *
     * <i>Note: PDU = Protocol Data Unit.</i>
     */
    public BandInfo(int id, byte[] data) {
        this.id = id;
        this.frequency = BytesUtils.getUINT16(data, FREQUENCY_OFFSET);
        this.q = BytesUtils.getUINT16(data, Q_OFFSET) / Q_FACTOR;
        this.filter = Filter.valueOf(BytesUtils.getUINT8(data, FILTER_OFFSET));
        this.gain = BytesUtils.getSINT16(data, GAIN_OFFSET) / GAIN_FACTOR;
    }

    public BandInfo(int id, int frequency, double q, Filter filter, double gain) {
        this.id = id;
        this.frequency = frequency;
        this.q = q;
        this.filter = filter;
        this.gain = gain;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getQ() {
        return q;
    }

    public Filter getFilter() {
        return filter;
    }

    public double getGain() {
        return gain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BandInfo bandInfo = (BandInfo) o;
        return id == bandInfo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static int formatGain(double value) {
        return (int) (value * GAIN_FACTOR);
    }

}
