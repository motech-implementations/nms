package org.motechproject.nms.rch.soap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationWebServices {
    public static  String getLocationResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        String response= "";
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String output;
            JSONObject obj = new JSONObject();

            while ((output = br.readLine()) != null) {
                response=response+output;
            }
            }
            con.disconnect();

        return response;
    }
}