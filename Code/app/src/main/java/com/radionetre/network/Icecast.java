package com.radionetre.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Icecast {

    protected static Map<String, String> parseMetadata(String icecastString) {
        // Store (K,V) metadata here
        Map<String, String> metadata = new HashMap();

        // Icecast metadata is a string like "StreamTitle='';StreamUrl='';..."
        String[] icecastProperties = icecastString.split(";");

        // Match this regex to find icecast properties like "StreamTitle=''"
        Pattern regexPattern = Pattern.compile("^([a-zA-Z]+)=\\'(.*)\\'$");

        Matcher regexMatcher;
        for (int i = 0; i < icecastProperties.length; i++)
        {
            regexMatcher = regexPattern.matcher(icecastProperties[i]);

            // Found a match?
            if (regexMatcher.find())
                metadata.put(regexMatcher.group(1), regexMatcher.group(2));
        }

        return metadata;
    }

    public static Map<String, String> getMetadata(URL streamUrl) throws IOException {
        URLConnection con = streamUrl.openConnection();

        con.setRequestProperty("Icy-MetaData", "1");
        con.setRequestProperty("Connection", "close");
        con.setRequestProperty("Accept", null);

        con.connect();

        int metadataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        if (headers.containsKey("icy-metaint")) {
            metadataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
        }
        // No metadata?
        if (metadataOffset == 0) {
            // Close HTTP stream
            stream.close();
            return null;
        }

        // Read metadata
        int aByte;
        int bytesCount = 0;
        int metadataLength = 4080; // 4080 is max possible length
        boolean inData = false;
        StringBuilder metadata = new StringBuilder();

        // Stream position should be either at the beginning or right after headers
        // stream.read() Reads the next byte of data from the input stream.
        while ((aByte = stream.read ()) != -1) {
            bytesCount++;

            // Length of the metadata
            if (bytesCount == metadataOffset + 1)
                metadataLength = aByte * 16;

            inData = bytesCount > metadataOffset + 1 &&
                     bytesCount < metadataOffset + metadataLength;

            if (inData)
                if (aByte != 0)
                    metadata.append((char) aByte);

            if (bytesCount > metadataOffset + metadataLength)
                break;

        }

        // Close HTTP stream
        stream.close();

        // Return all metadata
        return Icecast.parseMetadata(metadata.toString());
    }
}