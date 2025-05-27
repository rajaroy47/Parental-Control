package com.mycompany.myspyapp;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    public static final Set<String> blockedPackages = new HashSet<>();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        // Removed startForeground() to prevent crash on Android 11+
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String currentPackage = String.valueOf(event.getPackageName());
            if (blockedPackages.contains(currentPackage)) {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        // No-op
    }
}

