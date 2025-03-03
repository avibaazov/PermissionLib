# 📜 PermissionLib

A simple and powerful Android runtime permission library 🚀

Easily request and manage Android permissions with a fluent API. Handles explanations, denied cases, and forwarding users to settings.

## Features
- Simple API for permission requests
- Automatic handling of "never ask again" scenarios
- Built-in rationale dialogs for user explanations
- Seamless redirection to app settings for denied permissions




## Basic Usage

### Step 1: Declare Permissions in `AndroidManifest.xml`
Ensure that the required permissions are declared in your app's manifest:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.classy.permissionlib">

    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
</manifest>
```

### Step 2: Request Permissions in Code

Use **PermissionLib** to request runtime permissions within your activity:

```java
 permissionManager = new PermissionManager(this);
 permissionManager.permissions(Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE)
                    .request((allGranted, grantedPermissions, deniedPermissions) -> {
                        if (allGranted) {
                            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Denied: " + String.join(", ", deniedPermissions), Toast.LENGTH_LONG).show();
                        }
                    });
```

### Understanding the Callback
- **allGranted**: `true` if all requested permissions are granted, `false` otherwise.
- **grantedPermissions**: List of permissions that were granted.
- **deniedPermissions**: List of permissions that were denied.

---

## More Usage

### Handling Rationale Callbacks

Use **onExplainRequestReason** to handle situations where permissions are initially denied and provide a rationale before re-requesting:

```java
permissionManager.onExplainRequestReason((deniedPermissions, continueRequest) -> {
    // Implement your custom logic here (e.g., show a rationale dialog)
    continueRequest.run(); // Proceed with the permission request
}).request((allGranted, grantedPermissions, deniedPermissions) -> {
    if (!allGranted) {
        Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show();
    }
});
```

### Explanation:
- `onExplainRequestReason` is a callback triggered when a user **initially denies** a permission.
- It receives two parameters:
  - **deniedPermissions**: A list of permissions that were denied by the user.
  - **continueRequest**: A `Runnable` function that, when executed, **re-requests the permissions**.
- This allows you to define custom logic (e.g., displaying a rationale dialog) before continuing with the permission request.

---

## Handling Permanently Denied Permissions

Use **onForwardToSettings** when permissions are permanently denied ("Never ask again") and the user needs to be redirected to app settings.

```java
permissionManager.onForwardToSettings((deniedPermissions, openSettings) -> {
    // Implement your custom logic to inform the user that settings must be opened
    openSettings.run(); // Redirect the user to the app settings page
}).request((allGranted, grantedPermissions, deniedPermissions) -> {
    if (!allGranted) {
        Toast.makeText(this, "Some permissions were permanently denied", Toast.LENGTH_SHORT).show();
    }
});
```

### Explanation:
- `onForwardToSettings` is a callback triggered when permissions are **permanently denied** and can no longer be requested.
- It receives two parameters:
  - **deniedPermissions**: A list of permissions that were permanently denied.
  - **openSettings**: A `Runnable` function that, when executed, **redirects the user to the app settings page**.
- This allows you to define custom logic (e.g., displaying a message) before forwarding the user to settings.

---

## Explain Before Request

To improve user experience, always explain why permissions are needed **before** requesting them.

```java
permissionManager.explainReasonBeforeRequest();
```

This method ensures that the rationale dialog appears **before** the initial permission request, rather than only when a permission is denied.


With **PermissionLib**, handling Android runtime permissions is now seamless and efficient! 🚀
