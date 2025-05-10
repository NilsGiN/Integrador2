package com.example.prueba1.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LogosManager {
    private static Map<String, String> logosMap;

    public static void initialize(Context context) {
        if (logosMap != null) return;
        logosMap = new HashMap<>();

        try {
            InputStream is = context.getAssets().open("logos_marca.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("name").toLowerCase();
                JSONObject image = obj.getJSONObject("image");
                String url = image.getString("thumb");
                logosMap.put(name, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLogoUrl(String marca) {
        return logosMap.getOrDefault(marca.toLowerCase(), null);
    }
}