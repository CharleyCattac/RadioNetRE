package com.radionetre.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import com.radionetre.ui.activity.MainActivity;
import com.radionetre.ui.adapter.StationListAdapter;
import com.radionetre.R;
import com.radionetre.service.Settings;


public class LastPlayedFragment extends Fragment {

    Activity fragmentActivity;
    ListView listView;
    StationListAdapter stationListAdapter;

    ArrayList<HashMap<String, String>> lastPlayed;
    MainActivity.StationListItem stationListItem;

    public LastPlayedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The activity of this fragment
        this.fragmentActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View thisView = inflater.inflate(R.layout.fragment_played,
                                            container,
                                            false);

        this.listView = thisView.findViewById(R.id.played_list);

        // Retrieve list of stationsList
        this.loadLastPlayed();

        // Inflate the layout for this fragment
        return thisView;
    }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);
    }

    public void setStationListItem(MainActivity.StationListItem theCallback) {
        this.stationListItem = theCallback;
    }

    // Retrieve list from app preferences
    public void loadLastPlayed() {
        Settings settings = Settings.getInstance();
        this.lastPlayed = settings.retrieveLastPlayed(this.fragmentActivity);

        // Inflate ListView
        String[] from = {"name"};
        int[] to = { R.id.radio_name };

        this.stationListAdapter = new StationListAdapter(
            this.fragmentActivity,
            lastPlayed,
            R.layout.radio_list_item,
            from, to);

        this.listView.setAdapter(this.stationListAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HashMap<String, String> thisRadio = lastPlayed.get(position);

                if (stationListItem != null)
                    stationListItem.stream(thisRadio);
            }
        });
    }
}