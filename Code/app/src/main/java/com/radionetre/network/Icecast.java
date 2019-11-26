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

        /**
         * This header is required by Icecast. Without this header,
         * it won't return metadata (only audio stream).
         */
        con.setRequestProperty("Icy-MetaData", "1");

        /**
         * The Connection general header controls whether or not the
         * network connection stays open after the current transaction
         * finishes.
         * "close" indicates that either the client or the server
         * would like to close the connection.
         */
        con.setRequestProperty("Connection", "close");

        /**
         * The Accept request HTTP header advertises which content types,
         * expressed as MIME types, the client is able to understand.
         */
        con.setRequestProperty("Accept", null);

        con.connect();

        /**
         * For description of Shotcast protocol and "icy-metaint", see
         *
         * https://web.archive.org/web/20170624182814/http://www.smackfu.com/stuff/programming/shoutcast.html
         * https://web.archive.org/web/20170624182820/http://uniqueculture.net/2010/11/stream-metadata-plain-java/
         * https://web.archive.org/web/20170624182823/http://nicklothian.com/blog/2009/03/18/random-mp3-metadata-code/
         * https://web.archive.org/web/20170624181346/https://www.codeproject.com/Articles/11308/SHOUTcast-Stream-Ripper
         */
        int metadataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        /**
         * The icy-metaint: 32768 parameter is the most important value
         * for us, because it tells us the blocksize of the MP3 data.
         * In this example, the size of one MP3 block is 32768 bytes.
         * After the connection has been established, the stream starts
         * with an MP3 block of 32768 bytes. This block is followed by
         * one single byte, that indicates the size of the following
         * metadata block. This byte usually has the value 0, because
         * there is no metadata block after each MP3 block. If there is
         * a metadata block, the value of the single byte has to be
         * multiplied by 16 to get the length of the following metadata
         * block. The metadata block is followed by an MP3 block, the
         * single metadata length byte, eventually a metadata block,
         * an MP3 block, and so on (source: Shoutcast Metadata Protocol
         * by Scott McIntyre).
         */
        if (headers.containsKey("icy-metaint"))
        {
            metadataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
        } else {
            /**
             * The code below was used to retrieve icecast metadata if
             * the header "icy-metaint" was not set. Unfortunately it's
             * broken and it doesn't work. In particular, with HLS streams
             * (a protocol by Apple) there is no "icy-metaint" header.
             * Consequently, the code below will continue downloading
             * content indefinitely and eventually throw an "out of memory"
             * exception.
             *
             * Solution:
             *     - if header "icy-metaint" is set, download metadata
             *     - if header "icy-metaint" is not set, don't download any metadata
             */

            /*
            // Headers are sent within a stream
            StringBuilder strHeaders = new StringBuilder();

            char aChar;
            while ((aChar = (char) stream.read()) != -1)
            {
                strHeaders.append (aChar);

                // Detect end of metadata
                if (strHeaders.length() > 5 && (strHeaders.substring(strHeaders.length() - 4, strHeaders.length()).equals("\r\n\r\n")))
                    break;
            }

            // Match headers to get metadata offset within a stream
            Pattern regexPattern = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n");
            Matcher regexMatcher = regexPattern.matcher(strHeaders.toString());

            if (regexMatcher.find())
                metadataOffset = Integer.parseInt(regexMatcher.group(2));
            */
        }

        // No metadata?
        if (metadataOffset == 0)
        {
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
        while ((aByte = stream.read ()) != -1)
        {
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