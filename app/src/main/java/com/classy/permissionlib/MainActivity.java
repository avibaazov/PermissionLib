package com.classy.permissionlib;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;


import com.classy.perrmissionlib.*;


public class MainActivity extends AppCompatActivity {

    private PermissionManager permissionManager; // Register before the activity reaches RESUMED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize PermissionManager BEFORE activity reaches RESUMED state
        permissionManager = new PermissionManager(this);

        Button btnRequestPermission = findViewById(R.id.btnRequestPermission);
        btnRequestPermission.setOnClickListener(v -> {
            String storagePermission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            permissionManager
                    .permissions(storagePermission,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE)
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason((deniedPermissions, continueRequest) -> {
                        SystemIconRationaleDialog dialog = new SystemIconRationaleDialog(this, deniedPermissions);
                        dialog.setExplanationMessage("We need these permissions to enhance app functionality.");
                        dialog.setOnPositiveClick(continueRequest);
                        dialog.setOnNegativeClick(() -> {
                            // Handle case where user cancels
                        });
                        dialog.show();
                    })
                    .onForwardToSettings((deniedPermissions, openSettings) -> {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Permissions Permanently Denied")
                                .setMessage("These permissions are permanently denied: "
                                        + String.join(", ", deniedPermissions)
                                        + "\nPlease open app settings to grant them.")
                                .setPositiveButton("Open Settings", (dialog, which) -> openSettings.run())
                                .setNegativeButton("Cancel", null)
                                .show();
                    })
                    .request((allGranted, grantedPermissions, deniedPermissions) -> {
                        if (allGranted) {
                            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Denied: " + String.join(", ", deniedPermissions), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
