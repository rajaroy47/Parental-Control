package com.mycompany.myspyapp;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.List;

public class UsageStatsHelper {

    public static long getUsageTime(Context context, String packageName) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000L * 60 * 60 * 24); // last 24 hours

        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalTime = 0;

        if (stats != null) {
            for (UsageStats usageStats : stats) {
                if (usageStats.getPackageName().equals(packageName)) {
                    totalTime += usageStats.getTotalTimeInForeground();
                }
            }
        }

        return totalTime;
    }
}

