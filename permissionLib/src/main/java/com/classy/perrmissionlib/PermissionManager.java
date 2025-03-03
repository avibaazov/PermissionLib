package com.classy.perrmissionlib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private String[] permissions;
    private OnExplainRequestReasonCallback explainCallback;
    private OnForwardToSettingsCallback forwardCallback;
    private PermissionResultCallback resultCallback;
    private boolean explainBeforeRequest = false;
    private boolean hasShownExplanation = false;
    private boolean waitingForSettingsResult = false;

    public PermissionManager(AppCompatActivity activity) {
        this.activity = activity;

        // Register the permission launcher BEFORE activity reaches RESUMED state
        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );

        if (activity instanceof LifecycleOwner) {
            ((LifecycleOwner) activity).getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    if (waitingForSettingsResult) {
                        checkCurrentPermissionsAndCallback();
                    }
                }
            });
        }
    }

    public PermissionManager permissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public PermissionManager explainReasonBeforeRequest() {
        this.explainBeforeRequest = true;
        return this;
    }

    public PermissionManager onExplainRequestReason(OnExplainRequestReasonCallback callback) {
        this.explainCallback = callback;
        return this;
    }

    public PermissionManager onForwardToSettings(OnForwardToSettingsCallback callback) {
        this.forwardCallback = callback;
        return this;
    }

    public void request(PermissionResultCallback callback) {
        this.resultCallback = callback;

        if (arePermissionsGranted(activity, permissions)) {
            resultCallback.onResult(true, permissions, new String[0]);
            return;
        }

        if (explainBeforeRequest && !hasShownExplanation) {
            String[] denied = getDeniedPermissions(activity, permissions);
            hasShownExplanation = true;
            if (explainCallback != null) {
                explainCallback.onExplain(denied, () -> {
                    hasShownExplanation = false;
                    permissionLauncher.launch(permissions);
                });
            } else {
                SystemIconRationaleDialog dialog = new SystemIconRationaleDialog(activity, denied);
                dialog.setOnPositiveClick(() -> {
                    hasShownExplanation = false;
                    permissionLauncher.launch(permissions);
                });
                dialog.setOnNegativeClick(() -> {
                    resultCallback.onResult(false, new String[0], denied);
                });
                dialog.show();
            }
        } else {
            permissionLauncher.launch(permissions);
        }
    }

    private void handlePermissionResult(Map<String, Boolean> result) {
        List<String> grantedList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();
        List<String> permanentlyDeniedList = new ArrayList<>();

        for (String permission : permissions) {
            Boolean granted = result.get(permission);
            if (granted != null && granted) {
                grantedList.add(permission);
            } else {
                deniedList.add(permission);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    permanentlyDeniedList.add(permission);
                }
            }
        }

        boolean allGranted = grantedList.size() == permissions.length;

        if (!allGranted && !permanentlyDeniedList.isEmpty()) {
            waitingForSettingsResult = true;
            if (forwardCallback != null) {
                forwardCallback.onForward(permanentlyDeniedList.toArray(new String[0]), this::openAppSettings);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("Permissions Permanently Denied")
                        .setMessage("Some permissions have been permanently denied. Please open app settings to grant them.")
                        .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .show();
            }
        } else {
            resultCallback.onResult(allGranted,
                    grantedList.toArray(new String[0]),
                    deniedList.toArray(new String[0]));
            hasShownExplanation = false;
        }
    }

    private void checkCurrentPermissionsAndCallback() {
        List<String> grantedList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                grantedList.add(permission);
            } else {
                deniedList.add(permission);
            }
        }
        boolean allGranted = grantedList.size() == permissions.length;
        waitingForSettingsResult = false;
        resultCallback.onResult(allGranted,
                grantedList.toArray(new String[0]),
                deniedList.toArray(new String[0]));
    }

    private boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] getDeniedPermissions(Context context, String[] permissions) {
        List<String> denied = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                denied.add(permission);
            }
        }
        return denied.toArray(new String[0]);
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
