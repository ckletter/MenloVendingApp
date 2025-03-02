package com.example.menlovending.stripe.permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PermissionService {

    // Permissions (API Level 30 and below)
    private static final String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    private static final Map<String, String> permissionsTitle = new HashMap<>();

    static {
        permissionsTitle.put(Manifest.permission.ACCESS_FINE_LOCATION, "Fine Location");
        permissionsTitle.put(Manifest.permission.BLUETOOTH, "Bluetooth");
        permissionsTitle.put(Manifest.permission.BLUETOOTH_ADMIN, "Bluetooth Admin");
    }

    // Permission Status Enum
    public enum PermissionStatus {
        GRANTED,    // Permission Granted
        DENIED,     // Permission Denied (Can be requested)
        BLOCKED     // Permission Denied (Cannot be requested, must open settings)
    }

    // Permission Class
    public static class Permission {
        private String name;
        private String title;
        private PermissionStatus status;

        public Permission(String name, String title, PermissionStatus status) {
            this.name = name;
            this.title = title;
            this.status = status;
        }

        // Getter methods
        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public PermissionStatus getStatus() {
            return status;
        }
    }

    // Permission Checking Function
    public static List<Permission> checkPermissions(Context context) {
        List<Permission> permissionList = new ArrayList<>();
        String[] currentPermissions;
        Map<String, String> currentPermissionsTitle;
        currentPermissions = permissions;
        currentPermissionsTitle = permissionsTitle;
        for (int i = 0; i < currentPermissions.length; i++) {
            String permission = currentPermissions[i];
            PermissionStatus status;

            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                status = PermissionStatus.GRANTED;
            } else if (ActivityCompat.shouldShowRequestPermissionRationale((android.app.Activity) context, permission)) {
                status = PermissionStatus.BLOCKED;
            } else {
                status = PermissionStatus.DENIED;
            }

            permissionList.add(new Permission(permission, currentPermissionsTitle.get(permission), status));
        }

        return permissionList;
    }
}
