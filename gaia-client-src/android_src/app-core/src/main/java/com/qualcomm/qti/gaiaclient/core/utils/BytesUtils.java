/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <p>This class contains useful static methods to deal with values in <code>byte</code>.</p>
 */
public final class BytesUtils {

    /**
     * The tag to use when displaying logs.
     */
    private static final String TAG = "BytesUtils";

    /**
     * <p>The number of bytes contained in an int.</p>
     */
    private static final int BYTES_IN_INT = 4;
    /**
     * <p>The number of bits contained in a byte.</p>
     */
    public static final int BITS_IN_BYTE = 8;
    /**
     * The number of bytes to define a long.
     */
    private static final int BYTES_IN_LONG = 8;
    /**
     * The number of bytes that represents an INT16 number.
     */
    public static final int INT16_BYTES_LENGTH = 2;
    /**
     * The number of bytes that represents a INT32 number.
     */
    public static final int INT32_BYTES_LENGTH = 4;

    /**
     * To retrieve a UINT8 value from a Java signed byte value.
     *
     * @param source
     *         the java signed byte.
     *
     * @return An integer that represents the UINT8.
     */
    public static int getUINT8(byte source) {
        return source & 0xFF;
    }

    /**
     * To retrieve a UINT8 value from a byte array.
     *
     * @param source
     *         the array to get the UINT8 from.
     * @param offset
     *         the offset where the value starts.
     *
     * @return An integer that represents the UINT8.
     */
    public static int getUINT8(byte[] source, int offset) {
        return getUINT8(source, offset, 0);
    }

    /**
     * To retrieve a UINT8 value from a byte array.
     *
     * @param source
     *         the array to get the UINT8 from.
     * @param offset
     *         the offset where the value starts.
     * @param defaultValue
     *         the value to use if the source array was null or did not contain the offset.
     *
     * @return An integer that represents the UINT8.
     */
    public static int getUINT8(byte[] source, int offset, int defaultValue) {
        return source != null && source.length > offset ? source[offset] & 0xFF : defaultValue;
    }

    /**
     * To set a UINT8 value within a byte array.
     *
     * @param value
     *         the value to set into the array.
     * @param target
     *         the array that should receive the value.
     * @param offset
     *         the offset of the value starts.
     */
    public static void setUINT8(int value, byte[] target, int offset) {
        if (target != null && offset < target.length) {
            target[offset] = (byte) value;
        }
    }

    /**
     * To retrieve a UINT16 value from a byte array.
     *
     * @param source
     *         the array to get the UINT16 from.
     * @param offset
     *         the offset where the value starts.
     *
     * @return An integer that represents the UINT16.
     */
    public static int getUINT16(byte[] source, int offset) {
        return getHexValueFromByteArray(source, offset, INT16_BYTES_LENGTH);
    }

    /**
     * To set a SINT16 value within a byte array.
     *
     * @param value
     *         the value to set into the array.
     * @param target
     *         the array that should receive the value.
     * @param offset
     *         the offset of the value starts.
     *
     * @return the target array.
     */
    public static byte[] setSINT16(int value, byte[] target, int offset) {
        return putIntToByteArray(value, target, offset, INT16_BYTES_LENGTH);
    }

    /**
     * To set a UINT16 value within a byte array.
     *
     * @param value
     *         the value to set into the array.
     * @param target
     *         the array that should receive the value.
     * @param offset
     *         the offset of the value starts.
     *
     * @return the target array.
     */
    public static byte[] setUINT16(int value, byte[] target, int offset) {
        return putIntToByteArray(value, target, offset, INT16_BYTES_LENGTH);
    }

    /**
     * To retrieve a UINT32 value from a byte array.
     *
     * @param source
     *         the array to get the UINT32 from.
     * @param offset
     *         the offset where the value starts.
     *
     * @return A long that represents the UINT32.
     */
    public static long getUINT32(byte[] source, int offset) {
        return getLongFromByteArray(source, offset, INT32_BYTES_LENGTH);
    }

    /**
     * To set a UINT32 value within a byte array.
     *
     * @param value
     *         the long value to set into the array.
     * @param target
     *         the array that should receive the value.
     * @param offset
     *         the offset of the value starts.
     *
     * @return the target array.
     */
    public static byte[] setUINT32(long value, byte[] target, int offset) {
        return putLongToByteArray(value, target, offset, INT32_BYTES_LENGTH);
    }

    /**
     * To retrieve a SINT16 value from a byte array.
     *
     * @param source
     *         the array to get the SINT16 from.
     * @param offset
     *         the offset where the value starts.
     *
     * @return An integer that represents the SUINT16.
     */
    public static int getSINT8(byte[] source, int offset) {
        return source != null && source.length > offset ? source[offset] : 0;
    }

    /**
     * To retrieve a SINT16 value from a byte array.
     *
     * @param source
     *         the array to get the SINT16 from.
     * @param offset
     *         the offset where the value starts.
     *
     * @return An integer that represents the SUINT16.
     */
    public static int getSINT16(byte[] source, int offset) {
        return (short) getHexValueFromByteArray(source, offset, INT16_BYTES_LENGTH);
    }
    public static int getSINT32(byte[] source, int offset) {
        return getHexValueFromByteArray(source, offset, INT32_BYTES_LENGTH);
    }

    /**
     * <p>Extract a numerical value by its hexadecimal representation from a <code>bytes</code> array.</p>
     *
     * @param source
     *         The array to extract from.
     * @param offset
     *         Offset within source array.
     * @param length
     *         Number of bytes to use (maximum 4).
     *
     * @return The extracted value.
     */
    private static int getHexValueFromByteArray(byte[] source, int offset, int length) {
        if (rejectParamsForHexValue("getIntFromByteArray", source, offset, length, BYTES_IN_INT)) {
            // Insufficient parameters: issue logged by the check method
            return 0;
        }

        int result = 0;
        int shift = (length - 1) * BITS_IN_BYTE;

        for (int i = offset; i < offset + length; i++) {
            result |= (source[i] & 0xFF) << shift;
            shift -= BITS_IN_BYTE;
        }

        return result;
    }

    /**
     * Extract a <code>long</code> field from an array. This method treats the bytes in the array as unsigned.
     *
     * @param source
     *         The array to extract from.
     * @param offset
     *         Offset within source array.
     * @param length
     *         Number of bytes to use (maximum 8).
     *
     * @return The extracted integer.
     */
    private static long getLongFromByteArray(byte[] source, int offset, int length) {
        if (rejectParamsForHexValue("getLongFromByteArray", source, offset, length, BYTES_IN_LONG)) {
            // Insufficient parameters: issue logged by the check method
            return 0;
        }

        long result = 0;
        int shift = (length - 1) * BITS_IN_BYTE;

        for (int i = offset; i < offset + length; i++) {
            result |= (long) (source[i] & 0xFF) << shift;
            shift -= BITS_IN_BYTE;
        }

        return result;
    }

    /**
     * <p>Copies an int value into a byte array from the specified <code>offset</code> location to
     * the <code>offset + length</code> location.</p>
     *
     * @param sourceValue
     *         The <code>int</code> value to copy in the array.
     * @param target
     *         The <code>byte</code> array to copy the <code>int</code> value into.
     * @param targetOffset
     *         The targeted offset in the array to copy the first byte of the <code>int</code>
     *         value.
     * @param length
     *         The number of bytes in the array to copy the <code>int</code> value.
     *
     * @return the target array.
     */
    private static byte[] putIntToByteArray(int sourceValue, byte[] target, int targetOffset, int length) {
        if (rejectParamsForHexValue("putIntToByteArray", target, targetOffset, length, BYTES_IN_INT)) {
            // Insufficient parameters: issue logged by the check method
            return target;
        }

        int shift = (length - 1) * BITS_IN_BYTE;

        for (int i = 0; i < length; i++) {
            int mask = 0xFF << shift;
            target[i + targetOffset] = (byte) ((sourceValue & mask) >> shift);
            shift -= BITS_IN_BYTE;
        }
        return target;
    }

    /**
     * <p>Copies a long value into a byte array from the specified <code>offset</code> location to
     * the <code>offset + length</code> location.</p>
     *
     * @param sourceValue
     *         The <code>long</code> value to copy in the array.
     * @param target
     *         The <code>byte</code> array to copy the <code>long</code> value into.
     * @param targetOffset
     *         The targeted offset in the array to copy the first byte of the <code>int</code>
     *         value.
     * @param length
     *         The number of bytes in the array to copy the <code>long</code> value.
     *
     * @return the target array.
     */
    private static byte[] putLongToByteArray(long sourceValue, byte[] target, int targetOffset, int length) {
        if (rejectParamsForHexValue("putLongToByteArray", target, targetOffset, length,
                                    BYTES_IN_LONG)) {
            // Insufficient parameters: issue logged by the check method
            return target;
        }

        int shift = (length - 1) * BITS_IN_BYTE;

        for (int i = 0; i < length; i++) {
            int mask = 0xFF << shift;
            target[i + targetOffset] = (byte) ((sourceValue & mask) >> shift);
            shift -= BITS_IN_BYTE;
        }

        return target;
    }

    /**
     * Extract a sub array from the given array starting at the provided offset and finishing at
     * the last byte of the given array.
     *
     * @param source
     *         The array to extract an end array from.
     * @param offset
     *         The offset to start the sub array from.
     *
     * @return The sub array.
     */
    @NonNull
    public static byte[] getSubArray(byte[] source, int offset) {
        return getSubArray(source, offset, source.length - offset);
    }

    /**
     * <p>Extract a sub array from the given array starting at the given offset and of the
     * provided length.</p>
     *
     * @param source
     *         the array to extract a sub array from.
     * @param offset
     *         the offset to start the sub array from.
     * @param length
     *         the number of bytes to extract from the array.
     *
     * @return The sub array starting at the given offset and of the given length. Returns an
     *         empty array if the offset was out of bound of the array. If length+offset is out of bound,
     *         this method returns the sub array from the offset to the end of the array.
     */
    @NonNull
    public static byte[] getSubArray(byte[] source, int offset, int length) {
        if (rejectParamsForSubArray(source, offset, length)) {
            // Insufficient parameters to extract a sub array
            return new byte[0];
        }

        // if requested bytes are out of bound, only copy the inbound ones.
        if (source.length < offset + length) {
            length = source.length - offset;
        }

        byte[] result = new byte[length];
        System.arraycopy(source, offset, result, 0, length);
        return result;
    }

    /**
     * <p>Copies the content of the given source array into the given destination array from the provided destination
     * offset.</p>
     * <p>This method mostly provides an encapsulation of {@link System#arraycopy(Object, int, Object, int, int)} in
     * order to verify the arguments as follows:
     * <ul>
     *     <li>destination offset is within the bounds of the destination array.</li>
     *     <li>length is positive.</li>
     *     <li>destination can contain the source.</li>
     * </ul></p>
     *
     * @param source
     *         the array to extract a sub array from.
     * @param destination
     *         the offset to start the sub array from.
     * @param destinationOffset
     *         the number of bytes to extract from the array.
     */
    public static void copyArray(@NonNull byte[] source, @NonNull byte[] destination, int destinationOffset) {
        if (destinationOffset < 0 || destination.length <= destinationOffset) {
            // rejected: offset for destination is out of bound
            return;
        }

        if (destination.length < destinationOffset + source.length) {
            // destination does not contain enough data
            return;
        }

        System.arraycopy(source, 0, destination, destinationOffset, source.length);
    }

    /**
     * <p>Extracts a value from an integer from the given bit offset to the given length.</p>
     * <p><i>getValueFromBits(0b1010_1001_1010_0010, 7, 2) -> 0b11.</i></p>
     */
    public static int getValueFromBits(int source, int bitOffset, int length) {
        int mask = ((1 << length) - 1) << bitOffset;
        return (source & mask) >>> bitOffset;
    }

    /**
     * <p>Sets the bits of the value into the target from the bit offset to the length.</p>
     * <p><i>setValueAsBits(0b11, 0, 3) -> 0b1_1000.</i></p>
     */
    public static int setValueAsBits(int bitValues, int target, int bitOffset) {
        return target | (bitValues << bitOffset);
    }

    /**
     * Builds a String of encoding UTF-8 from the given bytes.
     *
     * @return an empty String is the array is null, empty or if no String could be identified from
     *         the given bytes.
     */
    public static String getString(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        String result = "";

        try {
            result = new String(data, StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            Log.w(TAG, "[getString] Exception when getting string: " + exception.getMessage());
            exception.printStackTrace();
        }

        return result;
    }

    /**
     * Convert a byte array to a human readable String.
     *
     * @param value
     *         The byte array.
     *
     * @return String object containing values in byte array formatted as hex.
     */
    public static String getHexadecimalStringFromBytes(byte[] value) {
        if (value == null) {
            return "null";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");

        //noinspection ForLoopReplaceableByForEach // for uses less ressources than foreach.
        for (int i = 0; i < value.length; i++) {
            stringBuilder.append(String.format("%02x ", value[i]));
        }
        return stringBuilder.toString();
    }

    /**
     * <p>This method allows retrieval of a human readable representation of an hexadecimal value
     * contained in a <code>int</code>.</p>
     *
     * @param i
     *         The <code>int</code> value.
     *
     * @return The hexadecimal value as a <code>String</code>.
     */
    public static String getHexadecimalStringFromInt(int i) {
        return String.format("%04X", i & 0xFFFF);
    }

    /**
     * To obtain the MD5 checksum of an Android Uri - likely to represent a file.
     *
     * @param uri
     *         The uri that describes the data to get a MD5 checksum for.
     */
    public static byte[] getMD5FromUri(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (inputStream != null && numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            return digest.digest();
        }
        catch (Exception e) {
            Log.e(TAG,
                  "Exception occurs when getting MD5 check sum for file: " + uri.getLastPathSegment());
            Log.e(TAG, "Exception: " + e.getMessage());
            return new byte[0];
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (Exception e) {
                    Log.w(TAG,
                          "Exception occurs when closing stream to get MD5 checksum: " + uri.getLastPathSegment());
                    Log.w(TAG, "Exception: " + e.getMessage());
                }
            }
        }
    }

    /**
     * To retrieve an array of bytes from an Android Uri - most likely to represent a file.
     *
     * @param uri
     *         The uri that corresponds to the data to obtain the bytes from.
     */
    public static byte[] getBytesFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.w(TAG, "[getBytesFromUri] inputStream is null for uri=" + uri);
                return null;
            }

            return getBytesFromInputStream(inputStream);
        }
        catch (Exception e) {
            Log.w(TAG,
                  "[getBytesFromUri] exception occurred for uri=" + uri + "\n" + e.getMessage());
            return null;
        }
    }

    /**
     * To read all the bytes of an InputStream and copy them into a byte array.
     */
    private static byte[] getBytesFromInputStream(InputStream inputStream) throws Exception {
        byte[] bytesResult;
        int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        try (ByteArrayOutputStream byteStreamBuffer = new ByteArrayOutputStream()) {
            while ((length = inputStream.read(buffer)) > 0) {
                byteStreamBuffer.write(buffer, 0, length);
            }
            bytesResult = byteStreamBuffer.toByteArray();
        }

        return bytesResult;
    }

    /**
     * <p>Checks that the given parameters match an extraction and/or copy of a UINT from/into a
     * byte array:
     * <ul>
     * <li>array is not null.</li>
     * <li>offset is within the array bounds.</li>
     * <li>length is within [0, lengthMax].</li>
     * <li>offset+length is within the array bounds.</li>
     * </ul></p>
     *
     * @param method
     *         The method this is called within for log purposes.
     * @param lengthMax
     *         the maximum number of bytes the length can be - this depends to the type
     *         of UINT (UINT8, UINT16, etc.).
     *
     * @return True if the given values matches the conditions.
     */
    private static boolean rejectParamsForHexValue(String method, byte[] array, int offset, int length, int lengthMax) {
        if (array == null) {
            Log.w(TAG, String.format("[%1$s] source array is null.", method));
            return true;
        }

        if (offset < 0 || array.length <= offset) {
            Log.w(TAG, String.format("[%1$s] Out of bound offset: offset=%2$s, source.length=%3$s",
                                     method, offset, array.length));
            return true;
        }

        if (length < 0 || lengthMax < length) {
            Log.w(TAG, String.format("[%1$s] Out of bound length: length=%2$s, expected=[0, %3$s].",
                                     method, length, lengthMax));
            return true;
        }

        if (array.length < offset + length) {
            Log.w(TAG, String.format("[%1$s] out of bound value: " +
                                             "offset=%2$s, length=%3$s, source.length=%4$s",
                                     method, offset, length, array.length));
            return true;
        }

        return false;
    }

    /**
     * <p>Checks that the given parameters match an extraction and/or copy of an array from/into a
     * byte array:
     * <ul>
     * <li>array is not null.</li>
     * <li>offset is within the array bounds.</li>
     * <li>length is not negative.</li>
     * <li>offset+length is within the array bounds.</li>
     * </ul></p>
     *
     * @param array
     *         the array to get the sub array from and/or the array to copy the sub array into.
     *
     * @return True if the given values matches the conditions.
     */
    private static boolean rejectParamsForSubArray(byte[] array, int offset, int length) {
        if (array == null) {
            // rejected: array is null
            return true;
        }

        if (offset < 0 || array.length <= offset) {
            // rejected: offset is out of bound
            return true;
        }

        //noinspection RedundantIfStatement
        if (length <= 0) {
            // rejected: length is negative or zero
            return true;
        }

        return false;
    }

}
