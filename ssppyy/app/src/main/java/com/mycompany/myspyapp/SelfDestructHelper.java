package com.mycompany.myspyapp;

import android.content.Context;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;

public class SelfDestructHelper {

    public static void deleteAppData(Context context) {
        try {
            // Delete internal files
            File dir = context.getFilesDir();
            deleteRecursive(dir);

            // Delete cache
            File cache = context.getCacheDir();
            deleteRecursive(cache);

            // Delete shared prefs
            File sharedPrefs = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            deleteRecursive(sharedPrefs);

            Log.d("SelfDestruct", "All app data deleted.");
        } catch (Exception e) {
            Log.e("SelfDestruct", "Failed to delete data: " + e.getMessage());
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) return;

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}

