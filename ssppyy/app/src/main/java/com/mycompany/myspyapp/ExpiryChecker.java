package com.mycompany.myspyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpiryChecker {

    private static final String TAG = "ExpiryChecker";
    private static final String PREF_EXPIRY_DATE_TIME = "expiryDateTime";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private Date expiryDateTime;
    private boolean isExpired = false;
    private final Handler handler = new Handler();
    private final Context context;
    private final Runnable expiryRunnable = new Runnable() {
        @Override
        public void run() {
            checkExpiry();
            if (!isExpired) {
                // Re-check every second (adjust as needed)
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
            } else {
                Log.i(TAG, "App has now expired (based on date/time).");
                // Optionally, stop the checking once expired
                // handler.removeCallbacks(this);
            }
        }
    };

    public ExpiryChecker(Context context) {
        this.context = context;
        loadExpiryDateTime();
        // Start checking for expiry immediately
        handler.post(expiryRunnable);
    }

    public void updateExpiryDateTime(String newExpiryDateTimeStr) {
        try {
            expiryDateTime = dateFormat.parse(newExpiryDateTimeStr);
            isExpired = new Date().after(expiryDateTime);
            saveExpiryDateTime(newExpiryDateTimeStr);
            Log.i(TAG, "Expiry date/time updated to: " + dateFormat.format(expiryDateTime));
            // If already expired, no need to keep checking
            if (isExpired) {
                stopCheckingExpiry();
            } else {
                // Ensure the checking is running if it was stopped
                handler.removeCallbacks(expiryRunnable);
                handler.post(expiryRunnable);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing new expiry date/time: " + e.getMessage());
            // Optionally handle the error, maybe revert to a default or keep the old value
        }
    }

    public boolean isExpired() {
        return isExpired;
    }

    private void checkExpiry() {
        if (expiryDateTime != null) {
            Date now = new Date();
            isExpired = now.after(expiryDateTime);
        } else {
            // Handle the case where expiryDateTime is null (parsing failed initially)
            isExpired = true; // Treat as expired
        }
    }

    private void loadExpiryDateTime() {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String storedDateTimeStr = prefs.getString(PREF_EXPIRY_DATE_TIME, null);
        if (storedDateTimeStr != null) {
            try {
                expiryDateTime = dateFormat.parse(storedDateTimeStr);
                isExpired = new Date().after(expiryDateTime);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing stored expiry date/time: " + e.getMessage());
                expiryDateTime = null;
                isExpired = true;
            }
        } else {
            // If no stored date, use the initial hardcoded values
            try {
                expiryDateTime = dateFormat.parse("13/04/2025" + " " + "16:23");
                isExpired = new Date().after(expiryDateTime);
                saveExpiryDateTime(dateFormat.format(expiryDateTime));
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing initial expiry date/time: " + e.getMessage());
                isExpired = true;
            }
        }
    }
    
    

    private void saveExpiryDateTime(String dateTimeStr) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_EXPIRY_DATE_TIME, dateTimeStr);
        editor.apply();
    }

    // You might want a method to stop the checking if needed
    public void stopCheckingExpiry() {
        handler.removeCallbacks(expiryRunnable);
        Log.i(TAG, "Expiry checking stopped.");
    }
    
    public String getExpiryDateTimeString() {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getString(PREF_EXPIRY_DATE_TIME, null);
    }
}
