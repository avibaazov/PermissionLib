package com.classy.perrmissionlib;


/**
 * Callback to handle permanently denied permissions.
 */
public interface OnForwardToSettingsCallback {
    /**
     * Called when some permissions are permanently denied.
     *

     * @param permanentlyDeniedPermissions The list of permanently denied permissions.
     */
    void onForward(String[] permanentlyDeniedPermissions, Runnable openSettings);
}
