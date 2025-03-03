package com.classy.perrmissionlib;

/**
 * Callback to explain why permissions are needed.
 */
public interface OnExplainRequestReasonCallback {
    /**
     * Called when the app should explain why permissions are needed.
     *

     * @param deniedPermissions The list of currently denied permissions.

     */
    void onExplain(String[] deniedPermissions, Runnable continueRequest);}
