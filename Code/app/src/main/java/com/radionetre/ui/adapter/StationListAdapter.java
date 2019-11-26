package com.radionetre.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.radionetre.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to create a station item
 */
public class StationListAdapter extends SimpleAdapter {
    RequestQueue queue;
    ArrayList<HashMap<String, String>> stations;

    public StationListAdapter(
            Context context,
            List<? extends Map<String, ?>> stations,
            int resource,
            String[] from, int[] to)
    {
        super(context, stations, resource, from, to);

        this.queue = Volley.newRequestQueue(context);
        this.stations = (ArrayList<HashMap<String, String>>) stations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        /**
         * Let SimpleAdapter built the view normally.
         * This will inflate text fields.
         */
        View v = super.getView(position, convertView, parent);

        // Now we need to custom-load the pictures
        NetworkImageView myNetworkImageView;
        ImageLoader myImageLoader;
        myNetworkImageView = (NetworkImageView) v.findViewById(R.id.radio_picture);

        String url = this.stations.get(position).get("logo");

        // Retrieve image
        if (url != null)
        {
            ImageLoader.ImageCache icache =
                new ImageLoader.ImageCache() {
                    // Documentation: https://developer.android.com/reference/android/util/LruCache.html
                     private final LruCache<String, Bitmap>
                         cache = new LruCache<String, Bitmap>(100);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                };

            myImageLoader = new ImageLoader(queue, icache);

            /* This code was in place to display a loading image
             * while the remote radio logo was being fetched.
             * The problem with this is that the NetworkImageView
             * components are recycled when scrolling the ListView.
             * The result is that pictures keep changing frantically
             * as HTTP requests return.
             *
             * I don't understand what's going on... so for the moment
             * this will stay commented out.
             *
            myImageLoader.get(
                url,
                ImageLoader.getImageListener(
                    myNetworkImageView,
                    0,     // Placeholder
                    0));   // Error
            */

            // Set the URL of the NetworkImageView
            myNetworkImageView.setImageUrl(url, myImageLoader);
        }

        return v;
    }
}