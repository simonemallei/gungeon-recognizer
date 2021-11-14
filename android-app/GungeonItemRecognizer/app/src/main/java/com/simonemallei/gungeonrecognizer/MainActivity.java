package com.simonemallei.gungeonrecognizer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;
import com.simonemallei.gungeonrecognizer.listener.OnQuerySearchListener;
import com.simonemallei.gungeonrecognizer.model.ApplicationModel;
import com.simonemallei.gungeonrecognizer.model.ItemModel;
import com.simonemallei.gungeonrecognizer.listener.OnItemSelectedListener;
import com.simonemallei.gungeonrecognizer.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.MainActivity
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * MainActivity class containing the main fragments of the application.
 */
public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, OnQuerySearchListener, OnRequestPermissionsResultCallback {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = MainActivity.class.getName();
    /**
     * A GeneralAdapter reference to the queryAdapter used by the activity.
     */
    private GeneralAdapter queryAdapter;
    /**
     * An integer containing the camera's permission code.
     */
    public static final int CAMERA_REQ_CODE = 100;
    /**
     * A SearchView reference to the SearchView that uses the queryAdapter.
     */
    public SearchView mSearchView = null;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Setting portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Setting Main Activity layout
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // Setting toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Performs the menu operation based on the MenuItem selected
     * @param item Item selected from the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_rate)
            openLink(getResources().getString(R.string.rate_link));
        else if (item.getItemId() == R.id.menu_apps)
            openLink(getResources().getString(R.string.apps_link));
        else if (item.getItemId() == R.id.menu_github)
            openLink(getResources().getString(R.string.github_link));
        else if (item.getItemId() == R.id.menu_privacy_policy)
            openLink(getResources().getString(R.string.privacy_policy_link));
        else if (item.getItemId() == R.id.menu_guide)
            showGuide();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    /**
     * Starts an intent to the browser with the parameter given as link.
     *
     * @param link String containing the link to open.
     */
    private void openLink(String link) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link)));
    }

    /**
     * Shows an alert with the User's guide.
     */
    private void showGuide() {
        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("App's Guide")
                .setMessage(getResources().getString(R.string.guide_msg))
                .setPositiveButton("OK", (dialogInterface, i) -> {}).create();
        mDialog.show();
    }

    @Override
    public void onItemSelected(ItemModel item) {
        // Opening ItemActivity based on the item clicked
        final Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("id", item.dbID);
        startActivity(intent);
    }

    public void setQueryAdapter(GeneralAdapter adapter) {
        this.queryAdapter = adapter;
    }

    // Actually not used, but necessary in order to implement the interface
    @Override
    public boolean onQueryTextSubmit(String s) {
        Log.i(TAG_LOG, "Text Submitted: "+ s);
        return false;
    }

    /**
     * Performs a select operation in the database using "LIKE" operator.
     * @param s String to search in the title, quote or description of an item.
     * @return True if the execution of the operation has been completed.
     */
    @Override
    public boolean onQueryTextChange(String s) {
        Log.i(TAG_LOG, "Query Text: " + s);
        // If the database is ready, performs the query
        if (ApplicationModel.isDbReady) {
            String tableName = ApplicationModel.TABLE_NAME;
            s = s.replace("'", "''");
            // Performing the text search query
            Cursor result = ApplicationModel.mDB.rawQuery("SELECT * FROM " + tableName +
                    " WHERE " +
                    "(TITLE LIKE '%" + s + "%' OR " +
                    "QUOTE LIKE '%" + s + "%' OR " +
                    "DESCRIPTION LIKE '%" + s + "%') ORDER BY DBID", null);
            int dbIDIndex = result.getColumnIndex("DBID");
            List<ItemModel> queryResult = new ArrayList<>();
            while (result.moveToNext()) {
                int dbID = result.getInt(dbIDIndex);
                queryResult.add(ApplicationModel.ITEMS.get(dbID - 1));
            }
            result.close();
            // Setting the query text model based on select results
            queryAdapter.setModel(queryResult);
            queryAdapter.notifyDataSetChanged();

            ListView mListView = findViewById(R.id.item_list);
            OnItemSelectedListener listener = this;
            // Setting the new click listener
            mListView.setOnItemClickListener(listener.getItemListener(queryAdapter));
        }
        return true;

    }

    /**
     * Return OnItemClickListener for the GeneralAdapter used.
     */
    public AdapterView.OnItemClickListener getItemListener(final GeneralAdapter adapter) {
        final OnItemSelectedListener listener = (OnItemSelectedListener) this;
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                ItemModel selectedItem = adapter.getItem(i);
                listener.onItemSelected(selectedItem);
            }
        };
    }
}