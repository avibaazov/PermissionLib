package com.classy.perrmissionlib;

/**
 * Callback that delivers the final result of the permission request.
 */
public interface PermissionResultCallback {
    /**
     * Called when the permission request cycle is finished.
     *
     * @param allGranted         True if all requested permissions are granted.
     * @param grantedPermissions List of granted permissions.
     * @param deniedPermissions  List of denied permissions.
     */
    void onResult(boolean allGranted, String[] grantedPermissions, String[] deniedPermissions);}