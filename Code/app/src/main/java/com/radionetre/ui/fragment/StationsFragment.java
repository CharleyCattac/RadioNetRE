package com.radionetre.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.radionetre.ui.activity.MainActivity;
import com.radionetre.R;
import com.radionetre.ui.adapter.StationListAdapter;
import com.radionetre.network.MetaDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class StationsFragment extends Fragment {

    Activity fragmentActivity;
    ListView listView;
    StationListAdapter stationListAdapter;
    LinearLayout loadingIcon;

    ArrayList<HashMap<String, String>> stationsList;
    RequestQueue apiRequestQueue;
    JsonObjectRequest apiRequest;

    StationListItem clickCallback;

    /*
     Remember last search query. Used for requesting more results
     when scrolling down.
     */
    String lastSearchQuery;

    // Used to ask more results
    int searchOffset;

    /*
     Keep track if all elements have been retrieved from the list.
     If we don't, the app would keep asking for more item forever.
     */
    boolean endOfList;

    public StationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // The activity of this fragment
        this.fragmentActivity = this.getActivity();

        this.apiRequestQueue = Volley.newRequestQueue(this.fragmentActivity);

        this.lastSearchQuery = "";
        this.searchOffset = 0;
        this.endOfList = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View thisView = inflater.inflate(R.layout.fragment_stations, container, false);

        this.listView = (ListView) thisView.findViewById(R.id.stations_list);
        this.loadingIcon = (LinearLayout) thisView.findViewById(R.id.search_loader);

        this.search();

        // Inflate the layout for this fragment
        return thisView;
    }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);
    }

    // Default search (all stationsList)
    public void search ()
    {
        this.search ("", false);
    }

    // Search called when new string is typed in the search box
    public void search (String query)
    {
        this.search (query, false);
    }

    /**
     * Send a new search request.
     *
     * @param query The text input by the user
     * @param more Should items be added at the bottom (more results)?
     */
    public void search (String query, final boolean more)
    {
        ConnectionChecker check = new ConnectionChecker();
        check.start();

        // When making a new search, reset endOfList
        if (!more)
        {
            this.searchOffset = 0;
            this.endOfList = false;
            this.listView.setOnScrollListener(null);
        }

        // If we reached end of list, do not search more.
        if (this.endOfList)
            return;

        this.lastSearchQuery = query;

        // Show loading icon
        this.loadingIcon.setVisibility(View.VISIBLE);

        // Cancel old request if any was sent
        if (apiRequest != null)
            apiRequest.cancel ();

        // Log.i("API", MetaDB.getStationsEndpoint (this.lastSearchQuery, this.searchOffset));

        // Create new request
        apiRequest = new JsonObjectRequest(
                Request.Method.GET,
                MetaDB.getStationsEndpoint (this.lastSearchQuery, this.searchOffset),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        updateStationsList (response, more);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        // Queue new request
        this.apiRequestQueue.add (this.apiRequest);
    }

    public void setClickCallback(MainActivity.StationListItem theCallback)
    {
        this.clickCallback = theCallback;
    }

    // Update list data after a search
    protected void updateStationsList (JSONObject data, boolean more) {
        JSONArray graph;

        /**
         * If requesting more results (instead of a new query),
         * they will be appended to the list, so we don't need
         * to initialize a new array.
         */
        if (!more)
            this.stationsList = new ArrayList<HashMap<String, String>>();

        // Parse returned JSON
        try {

            graph = data.getJSONArray ("@graph");

            // No more elements returned, stop requesting more stuff.
            if (graph.length() == 0)
            {
                this.endOfList = true;
                this.loadingIcon.setVisibility (View.GONE);

                /**
                 * If this is a call to request more results, quit
                 * here because there isn't anything new.
                 * If this was a new request, go on, such that the list
                 * will be emptied (0 results).
                 */
                if (more)
                    return;
            }

            for (int i = 0; i < graph.length(); i++)
            {
                JSONObject aRadio = graph.getJSONObject(i);

                if (aRadio.has ("@id") &&
                    aRadio.has ("schema:name") &&
                    aRadio.has ("schema:url"))
                {
                    HashMap<String, String> aStationValues = new HashMap<>();

                    aStationValues.put ("id", aRadio.getString("@id"));
                    aStationValues.put ("name", aRadio.getString("schema:name"));
                    aStationValues.put ("stream", aRadio.getJSONObject("schema:url").getString("@id"));

                    if (aRadio.has ("schema:logo"))
                        aStationValues.put ("logo", aRadio.getJSONObject("schema:logo").getString("@id"));
                    else
                        aStationValues.put ("logo", null);

                    this.stationsList.add (aStationValues);
                }
            }

        } catch (Exception e) {}

        // Increment offset for next request
        this.searchOffset += MetaDB.LOAD_LIMIT;

        String[] from = {"name"};
        int[] to = {R.id.radio_name};

        if (more)
        {
            this.stationListAdapter.notifyDataSetChanged();
        } else {
            this.stationListAdapter = new StationListAdapter(
                    this.fragmentActivity,
                    this.stationsList,
                    R.layout.radio_list_item,
                    from, to);

            this.listView.setAdapter(this.stationListAdapter);

            // List item clicked?
            this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, String> thisRadio = stationsList.get(position);

                    if (clickCallback != null)
                        clickCallback.stream(thisRadio);
                }
            });

            // Detect when scrolling reaches the end of the list
            this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                // The minimum amount of items to have below current scroll position, before loading more.
                private int visibleThreshold = 10;

                // The current page of data you have loaded
                private int currentPage = 0;

                // Total number of items loaded after the last load
                private int previousTotal = 0;

                // True if we are still waiting for the last set of data to load.
                private boolean loading = true;

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {

                    if (endOfList)
                        return;

                    if (loading)
                    {
                        if (totalItemCount > previousTotal)
                        {
                            loading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold))
                        search (lastSearchQuery, true);
                }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
            });
        }

        // Hide loading icon
        this.loadingIcon.setVisibility(View.GONE);
    }

    private class ConnectionChecker extends Thread {
        private boolean isNetworkConnected() {
            ConnectivityManager cm = (ConnectivityManager)getActivity()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected()
                    && cm.getActiveNetworkInfo().isAvailable();
        }

        private boolean isConnectionAvailable() {
            try {
                InetAddress address = InetAddress.getByName("google.com");
                return !address.equals("");
            } catch (UnknownHostException e) {
                return false;
            }
        }

        public void run() {
            String msg;

            if (!isNetworkConnected()) {
                msg = "Network not connected. Check it and try again later";
            } else if (!isConnectionAvailable()) {
                msg = "Internet access not available. Try again later";
            } else {
                return;
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Network connection error")
                    .setMessage(msg)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    System.exit(1);
                                }
                            });
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }
}