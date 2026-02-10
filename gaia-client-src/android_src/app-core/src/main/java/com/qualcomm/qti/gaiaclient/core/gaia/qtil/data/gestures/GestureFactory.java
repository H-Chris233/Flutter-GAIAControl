/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public final class GestureFactory {

    @NonNull
    public static Gesture getGesture(int id) {
        Gesture gesture = QTILGestures.valueOf(id);
        return gesture != null ? gesture : new GestureDefault(id);
    }

    @NonNull
    public static GestureContext getGestureContext(int id) {
        GestureContext gesture = QTILGestureContexts.valueOf(id);
        return gesture != null ? gesture : new GestureContextDefault(id);
    }

    @NonNull
    public static Action getAction(int id) {
        Action action = QTILActions.valueOf(id);
        return action != null ? action : new ActionDefault(id);
    }

    @NonNull
    public static Set<Gesture> getGestures(byte[] data) {
        return getValues(data, new GestureContract());
    }

    @NonNull
    public static Set<GestureContext> getGestureContexts(byte[] data) {
        return getValues(data, new ContextContract());
    }

    @NonNull
    public static Set<Action> getActions(byte[] data) {
        return getValues(data, new ActionContract());
    }

    @NonNull
    private static <E extends BitItem> Set<E> getValues(byte[] data, FactoryContract<E> contract) {
        if (data == null || data.length == 0) {
            return Collections.emptySet();
        }

        Set<E> values = new LinkedHashSet<>();
        values.addAll(getQTILValues(data, contract));
        values.addAll(getDefaultValues(data, contract));

        return values;
    }

    @NonNull
    private static <E extends BitItem> Set<E> getQTILValues(byte[] data, FactoryContract<E> contract) {
        Stream<E> stream = Arrays.stream(contract.getQTILValues());
        Stream<E> result = stream.filter(field -> field.isPresent(data));
        return result.collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NonNull
    private static <E extends BitItem> Set<E> getDefaultValues(byte[] data, FactoryContract<E> contract) {
        Set<E> fields = new LinkedHashSet<>();

        int startByteOffset = contract.getMaximumByteOffset();
        int minBitOffset = contract.getMaximumBitOffset();

        for (int byteOffset = startByteOffset; byteOffset < data.length; byteOffset++) {
            int value = getUINT8(data, byteOffset, 0);

            int start = byteOffset == startByteOffset ? minBitOffset + 1 : 0;
            for (int bitOffset = start; bitOffset < BITS_IN_BYTE; bitOffset++) {
                int mask = 1 << bitOffset;
                if ((value & mask) > 0) {
                    fields.add(contract.createDefault(byteOffset, bitOffset));
                }
            }
        }

        return fields;
    }

    private interface FactoryContract<E extends BitItem> {

        @NonNull
        E[] getQTILValues();

        @NonNull
        E createDefault(int byteOffset, int bitOffset);

        int getMaximumByteOffset();

        int getMaximumBitOffset();
    }

    private static class GestureContract implements FactoryContract<Gesture> {

        @NonNull
        @Override
        public QTILGestures[] getQTILValues() {
            return QTILGestures.values();
        }

        @NonNull
        @Override
        public GestureDefault createDefault(int byteOffset, int bitOffset) {
            return new GestureDefault(byteOffset, bitOffset);
        }

        @Override
        public int getMaximumByteOffset() {
            return QTILGestures.getMaximumByteOffset();
        }

        @Override
        public int getMaximumBitOffset() {
            return QTILGestures.getMaximumBitOffset();
        }
    }

    private static class ContextContract implements FactoryContract<GestureContext> {

        @NonNull
        @Override
        public QTILGestureContexts[] getQTILValues() {
            return QTILGestureContexts.values();
        }

        @NonNull
        @Override
        public GestureContextDefault createDefault(int byteOffset, int bitOffset) {
            return new GestureContextDefault(byteOffset, bitOffset);
        }

        @Override
        public int getMaximumByteOffset() {
            return QTILGestureContexts.getMaximumByteOffset();
        }

        @Override
        public int getMaximumBitOffset() {
            return QTILGestureContexts.getMaximumBitOffset();
        }
    }

    private static class ActionContract implements FactoryContract<Action> {

        @NonNull
        @Override
        public QTILActions[] getQTILValues() {
            return QTILActions.values();
        }

        @NonNull
        @Override
        public ActionDefault createDefault(int byteOffset, int bitOffset) {
            return new ActionDefault(byteOffset, bitOffset);
        }

        @Override
        public int getMaximumByteOffset() {
            return QTILActions.getMaximumByteOffset();
        }

        @Override
        public int getMaximumBitOffset() {
            return QTILActions.getMaximumBitOffset();
        }
    }
}
