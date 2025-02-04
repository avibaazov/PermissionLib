package com.classy.permissionlib;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class PermissionManager {

    // Define a callback interface for permission results
    public interface PermissionCallback {
        /**
         * Called when the permission request is completed.
         * @param granted True if all permissions are granted.
         * @param permanentlyDenied True if any permission was permanently denied.
         */
        void onPermissionResult(boolean granted, boolean permanentlyDenied);
    }

    private static final int PERMISSION_REQUEST_CODE = 100;

    // These static fields help us track the ongoing request.
    // (For a production-level library, you might want a better mechanism that supports multiple requests.)
    private static PermissionCallback mCallback;
    private static String[] mPermissions;
    private static Activity mActivity;

    /**
     * Requests the specified permissions.
     *
     * @param activity    The Activity from which permissions are requested.
     * @param permissions Array of permissions to request.
     * @param callback    A callback that returns the result.
     */
    public static void requestPermissions(Activity activity, String[] permissions, PermissionCallback callback) {
        mActivity = activity;
        mPermissions = permissions;
        mCallback = callback;

        // First, check if all permissions are already granted
        if (arePermissionsGranted(activity, permissions)) {
            if (mCallback != null) {
                mCallback.onPermissionResult(true, false);
            }
        } else {
            // Check if we should show a rationale for any permission
            boolean shouldShowRationale = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }
            if (shouldShowRationale) {
                showRationaleDialog(activity);
            } else {
                // Request the permissions directly if no rationale is needed
                ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Checks whether all the given permissions are granted.
     */
    private static boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Displays a rationale dialog explaining why the app needs the permissions.
     */
    private static void showRationaleDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Permission Required")
                .setMessage("This app needs these permissions to work properly. Please grant the permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Once the user agrees, request the permissions.
                        ActivityCompat.requestPermissions(activity, mPermissions, PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled the request.
                        if (mCallback != null) {
                            mCallback.onPermissionResult(false, false);
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * Call this method from your Activity's onRequestPermissionsResult to forward the result.
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            boolean permanentlyDenied = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    // If the user checked "Don't ask again", shouldShowRequestPermissionRationale returns false.
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[i])) {
                        permanentlyDenied = true;
                    }
                }
            }
            if (mCallback != null) {
                mCallback.onPermissionResult(allGranted, permanentlyDenied);
            }
        }
    }

    /**
     * Optional: A method to open the app's settings screen if permissions are permanently denied.
     * (Implementation depends on your needs.)
     */
    public static void openAppSettings(Activity activity) {
        // You can implement this to direct the user to the app's settings screen.
    }
}