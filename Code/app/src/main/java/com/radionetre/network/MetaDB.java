package com.radionetre.network;

import java.net.URLEncoder;

public class MetaDB {

    public static final String API_ENDPOINT = "https://api.metadb.peers.community/";
    public static final int LOAD_LIMIT = 30;

    // URL to retrieve list of radio stations
    public static String getStationsEndpoint (String query, int offset)
    {
        String url =
            MetaDB.API_ENDPOINT +
            "json-ld/radio-browser/stations" +
            "?limit=" + MetaDB.LOAD_LIMIT +
            "&offset=" + offset;

        try {
            url += "&name=" + URLEncoder.encode (query, "UTF-8");
        } catch (Exception e)
        {}

        return url;
    }

    public static String getStationsEndpoint (String query)
    {
        return MetaDB.getStationsEndpoint (query, 0);
    }
}