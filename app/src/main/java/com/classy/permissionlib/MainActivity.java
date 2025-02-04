package com.classy.permissionlib;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request CAMERA permission using our PermissionManager helper
        PermissionManager.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionResult(boolean granted, boolean permanentlyDenied) {
                        if (granted) {
                            Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                            // You can now proceed with using the camera.
                        } else if (permanentlyDenied) {
                            Toast.makeText(MainActivity.this, "Permission permanently denied. Please enable it in settings.", Toast.LENGTH_SHORT).show();
                            // Optionally: PermissionManager.openAppSettings(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    // Forward the permission result to our PermissionManager helper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
