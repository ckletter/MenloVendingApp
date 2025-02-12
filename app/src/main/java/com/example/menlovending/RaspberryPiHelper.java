package com.example.menlovending;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RaspberryPiHelper {
    public static void sendSignalToPi(String piIpAddress) {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + piIpAddress + ":5000/trigger");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                String jsonInputString = "{\"message\": \"Hello from Android!\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
