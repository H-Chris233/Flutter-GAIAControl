/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.devicelogs;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressFragment;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressFragmentViewData;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DownloadLogsFragment extends ProgressFragment<LogsDialog, Void, DownloadLogsViewModel> {

    private final ActivityResultLauncher<Intent> shareFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> navigateBack());

    @Override
    protected DownloadLogsViewModel initViewModel(Fragment fragment) {
        return new ViewModelProvider(this).get(DownloadLogsViewModel.class);
    }

    @Override
    protected ProgressFragmentViewData getProgressFragmentViewData() {
        return new ProgressFragmentViewData(getString(R.string.button_cancel), getString(R.string.button_share));
    }

    @Override
    protected void onEvent(NavigationEvent event) {
        switch (event.getType()) {
            case NAVIGATE_BACK:
                navigateBack();
                break;

            case START_ACTIVITY:
                ShareFileNavigationEvent shareFileEvent = (ShareFileNavigationEvent) event;
                startShareFileActivity(shareFileEvent);
                break;
        }
    }

    private void navigateBack() {
        Navigator.navigate(getRoot(), DownloadLogsFragmentDirections.back());
    }

    private void startShareFileActivity(ShareFileNavigationEvent event) {
        // create intent
        Intent sendIntent = new Intent(Intent.ACTION_SEND);

        // add logs file
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.putExtra(Intent.EXTRA_STREAM, event.getUri());
        sendIntent.setType("*/*"); // type not recognised by Android -> using "anything"

        // start system sharing action
        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.logs_share_intent_title));

        shareFileLauncher.launch(shareIntent);

        /*
        Note: When the contract is launched, the following harmless permission exception can be seen in the system logs:
            E/DatabaseUtils: Writing exception to parcel
            java.lang.SecurityException: Permission Denial: reading androidx.core.content.FileProvider uri
                content://com.qualcomm.qti.gaiaclient.fileprovider/all_files/device_logs.xed from
                pid=----, uid=---- requires the provider be exported, or grantUriPermission()
            at android.content.ContentProvider.enforceReadPermissionInner(ContentProvider.java:729)
            at android.content.ContentProvider$Transport.enforceReadPermission(ContentProvider.java:602)
            at android.content.ContentProvider$Transport.query(ContentProvider.java:231)
            at android.content.ContentProviderNative.onTransact(ContentProviderNative.java:104)
            at android.os.Binder.execTransactInternal(Binder.java:1021)
            at android.os.Binder.execTransact(Binder.java:994)

        The exception seems to be a race condition when the system handles the contract. The system creates the
         parcelable information to share - that includes information from the Uri - and adding
         the permission flag. In other words, the system tries to get information about the file
         before to be granted access. A possible workaround is to call:
            getContext().grantUriPermission("android", event.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);

         This has been seen with Android 10 and Android 11, it might also happened with other versions.
         */
    }
}
