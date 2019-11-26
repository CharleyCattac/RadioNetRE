package com.radionetre.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.radionetre.R;
import com.radionetre.ui.adapter.ViewPagerAdapter;
import com.radionetre.ui.fragment.LastPlayedFragment;
import com.radionetre.ui.fragment.StationsFragment;
import com.radionetre.play.Player;
import com.radionetre.record.Recorder;
import com.radionetre.service.Settings;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /**
     * Used as callback from fragments when clicking an item,
     * to upload the list of last played stations.
     */
    public interface StationListItem {
        // Start streaming
        void stream(HashMap<String, String> stationItem);
    }

    protected Toolbar toolbar;
    protected TabLayout tabLayout;
    protected ViewPager viewPager;
    protected Menu appMenu;
    protected EditText layout_search_box;

    protected boolean search_box_open = false;
    protected StationsFragment stationsFragment;
    protected LastPlayedFragment lastPlayedFragment;

    protected Player player;
    protected Recorder recorder;
    protected Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.layout_search_box = findViewById(R.id.search_box);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        this.viewPager = findViewById(R.id.viewpager);
        this.setupViewPager(this.viewPager);

        this.tabLayout = findViewById(R.id.tabs);
        this.tabLayout.setupWithViewPager(this.viewPager);

        this.player = Player.getInstance();
        this.recorder = Recorder.getInstance();
        this.settings = Settings.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        this.appMenu = menu;

        // Action bar title and logo
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                this.toggleSearchBar();
                return true;
            case R.id.menu_item_record:
                this.recordStream();
                return true;
            case R.id.menu_item_clear_list:
                settings.emptyLastPlayedList(this);
                lastPlayedFragment.loadLastPlayed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Setup page tabs
    protected void setupViewPager(ViewPager viewPager) {
        // Adapter for the page tabs
        ViewPagerAdapter adapter = new ViewPagerAdapter(this.getSupportFragmentManager());

        StationListItem clickCallback = new StationListItem() {
            @Override
            public void stream(HashMap<String, String> stationItem)
            {
                player.playStream(MainActivity.this, stationItem);

                // Save radio to list of last played
                settings.addNewPlayedRadio(MainActivity.this, stationItem);

                // Reload last played radios in fragment
                lastPlayedFragment.loadLastPlayed();
            }
        };

        this.stationsFragment = new StationsFragment();
        this.lastPlayedFragment = new LastPlayedFragment();

        this.stationsFragment.setClickCallback(clickCallback);
        this.lastPlayedFragment.setStationListItem(clickCallback);

        adapter.addFragment(this.stationsFragment, getString(R.string.stations));
        adapter.addFragment(this.lastPlayedFragment, getString(R.string.last_played));

        viewPager.setAdapter(adapter);

        recorder.init(this);
    }

    // Hide/Show search bar
    private void toggleSearchBar() {
        // Searching will be done in the actionbar.
        ActionBar action = this.getSupportActionBar();

        if (search_box_open) {
            // Close the search or delete the search query.
            if (layout_search_box.getText().toString().equals("")) {
                action.setDisplayShowCustomEnabled(true);

                // Change search icon from X to lens.
                this.appMenu
                        .findItem(R.id.menu_item_search)
                        .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_search));

                // Remove keyboard
                loseFocusSearchBox();

                // Remove the search view and display the title again.
                action.setCustomView(null);
                action.setDisplayShowTitleEnabled(true);

                // Keep track that the search box is now closed.
                search_box_open = false;
            } else {
                layout_search_box.setText("");
            }
        } else {
            // Replace the title with a search bar.
            action.setDisplayShowCustomEnabled(true);
            action.setDisplayShowTitleEnabled(false);
            action.setCustomView(R.layout.search_bar);

            layout_search_box = findViewById (R.id.search_box);
            layout_search_box.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    stationsFragment.search(layout_search_box.getText().toString());
                }

                @Override
                public void beforeTextChanged(CharSequence cs, int s, int b, int c) {
                }

                @Override
                public void onTextChanged(CharSequence cs, int s, int b, int c) {
                }
            });
            this.focusSearchBox ();

            // Change icon from lens to X.
            this.appMenu
                    .findItem(R.id.menu_item_search)
                    .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));

            // Keep track that the search box is now open
            search_box_open = true;
        }
    }

    // Move focus to search bar and open the keyboard
    private void focusSearchBox() {
        layout_search_box.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.layout_search_box, InputMethodManager.SHOW_IMPLICIT);
    }

    // Close the keyboard
    private void loseFocusSearchBox()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.layout_search_box.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    //Start recording stream
    private void recordStream() {
        recorder.record();
    }
}