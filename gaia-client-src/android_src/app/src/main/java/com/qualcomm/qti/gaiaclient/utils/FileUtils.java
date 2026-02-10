/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils;

import static java.util.Collections.emptyList;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.qualcomm.qti.gaiaclient.BuildConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class FileUtils {

    public static String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static File createFile(Context context, String subfolder, String fileName) {
        File imagePath = new File(context.getFilesDir(), subfolder);
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }

        return new File(imagePath, fileName);
    }

    public static Uri getUriFromFile(Context context, File file, String fileProviderAuthority) {
        return file != null ? FileProvider.getUriForFile(context, fileProviderAuthority, file) : null;
    }

    public static Boolean deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public static List<File> listFilesInDirectory(Context context, String subfolder) {
        File file = new File(context.getFilesDir(), subfolder);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return Arrays.asList(files);
            }
        }
        return emptyList();
    }
}
