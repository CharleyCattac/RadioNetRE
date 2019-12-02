package com.radionetre.network;

import java.net.URLEncoder;

public class MetaReceiver {

    public static final String API_ENDPOINT = "https://api.metadb.peers.community/";
    public static final int LOAD_LIMIT = 30;

    // URL to retrieve list of radio stations
    public static String getStationsEndpoint(String query, int offset) {
        String url =
            API_ENDPOINT +
            "json-ld/radio-browser/stations" +
            "?limit=" + LOAD_LIMIT +
            "&offset=" + offset;

        try {
            url += "&name=" + encode (query, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getStationsEndpoint(String query) {
        return getStationsEndpoint (query, 0);
    }
}