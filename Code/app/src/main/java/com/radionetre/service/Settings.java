package com.radionetre.service;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manage list of last played radios.
 * Store and retrieve from SharedStorage.
 */

public class Settings {

    //singleton implementation
    private static Settings instance = new Settings();

    private Settings() {
    }

    public static Settings getInstance() {
        return instance;
    }

    // Name of the property to use for storing the list of radios.
    private static final String SETTINGS_LAST_PLAYED = "last_played";

    // Name of the property to use for storing the notification settings.
    private static final String SETTINGS_PLAYER = "player_settings";

    // How many items should the list remember max.
    private static final int MAX_ITEMS = 30;

    public ArrayList<HashMap<String, String>> retrieveLastPlayed(Activity context) {
        // Used to serialize Java objects to Json
        Gson gson = new Gson();

        // Get the app preferences
        SharedPreferences appPreferences = context.getPreferences(context.MODE_PRIVATE);

        ArrayList<HashMap<String, String>> lastStations =
                new ArrayList<HashMap<String, String>>();

        // Retrieve list of last played stations
        String lastStationsGson = appPreferences.getString(Settings.SETTINGS_LAST_PLAYED, null);

        // Convert Json to Java object
        if (lastStationsGson != null)
            lastStations = gson.fromJson(
                    lastStationsGson,
                    new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType());

        return lastStations;
    }

    public HashMap<String, Object> retrievePlayerSettings(Activity context) {
        // Used to serialize Java objects to Json
        Gson gson = new Gson();

        // Get the settings
        SharedPreferences appPreferences = context.getPreferences(context.MODE_PRIVATE);

        HashMap<String, Object> settings = new HashMap<String, Object>();

        String settingsPreferences = appPreferences.getString(Settings.SETTINGS_PLAYER, null);

        // Convert Json to Java object
        if (settingsPreferences != null)
            settings = gson.fromJson(
                    settingsPreferences,
                    new TypeToken<HashMap<String, Object>>(){}.getType());

        return settings;
    }

    public void addNewPlayedRadio(Activity context, HashMap<String, String> aRadio) {
        // Used to serialize Java objects to Json
        Gson gson = new Gson();

        // Get the app preferences
        SharedPreferences appPreferences = context.getPreferences(context.MODE_PRIVATE);

        // Get stations
        ArrayList<HashMap<String, String>> lastStations = retrieveLastPlayed(context);

        while (lastStations.remove(aRadio));

        // Add radio at the top of list
        lastStations.add(0, aRadio);

        // Cut off list to prevent it from growing too much
        if (lastStations.size() > Settings.MAX_ITEMS)
            lastStations.subList(Settings.MAX_ITEMS, lastStations.size()).clear();

        // Re-serialize list of radios and save preference
        appPreferences
            .edit()
            .putString(Settings.SETTINGS_LAST_PLAYED, gson.toJson(lastStations))
            .apply();
    }

    public void emptyLastPlayedList(Activity context) {
        // Get the app preferences
        SharedPreferences appPreferences = context.getPreferences(context.MODE_PRIVATE);

        // Re-serialize list of radios and save preference
        appPreferences
                .edit()
                .remove(Settings.SETTINGS_LAST_PLAYED)
                .apply();
    }
}