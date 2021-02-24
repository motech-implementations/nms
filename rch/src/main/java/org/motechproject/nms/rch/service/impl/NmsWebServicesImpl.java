package org.motechproject.nms.rch.service.impl;

import org.motechproject.nms.rch.service.NmsWebServices;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service("nmsWebServices")
public class NmsWebServicesImpl implements NmsWebServices{
    @Override
    public  String getLocationApiResponse(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        StringBuilder response= new StringBuilder();

        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
        }
        con.disconnect();

        return response.toString();
    }
}