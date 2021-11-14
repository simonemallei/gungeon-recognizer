package com.simonemallei.gungeonrecognizer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simonemallei.gungeonrecognizer.model.ApplicationModel;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.ItemActivity
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ItemActivity class containing activity created when the application has to show
 * details of an item.
 */
public class ItemActivity extends AppCompatActivity {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = ItemActivity.class.getName();
    /**
     * An ItemModel reference to the item shown.
     */
    private ItemModel mItem = null;
    /**
     * An array of String objects of attributes containing items ids in a synergy object.
     */
    private final String [] ID_KEYS = {"all_id", "ootf_id", "totf_id", "ootf_2_id"};
    /**
     * An array of String objects of attributes containing items names in a synergy object.
     */
    private final String [] NAME_KEYS = {"all", "ootf", "totf", "ootf_2"};
    /**
     * An array of String objects containing the String to show in the ItemActivity based
     * on the type of the items for that synergy.
     */
    private final String [] SYN_STRING = {"Necessary: ", "One of the following: ", "Two of the following:", "One of the following: "};

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent srcIntent = getIntent();
        if (srcIntent != null) {

            int item_ID = srcIntent.getIntExtra("id", 1);
            // If item's ID is greater than model's size, goes back to the previous activity
            if (item_ID > ApplicationModel.ITEMS.size()) {
                Log.i(TAG_LOG, "Model's size: " + ApplicationModel.ITEMS.size());
                Log.i(TAG_LOG, "ID requested: " + item_ID);
                this.onBackPressed();
            }
            mItem = ApplicationModel.ITEMS.get(item_ID - 1);
        }
        createView();
    }

    /**
     * Displays all the available details about that item.
     */
    public void createView(){

        // Setting Item Activity layout
        setContentView(R.layout.activity_item);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Setting scaled image
        Drawable mImage = mItem.image;
        Bitmap mBitmap = ((BitmapDrawable)mImage).getBitmap();

        int dimensionScaled = displayMetrics.widthPixels / 4;

        Bitmap mBitmapResized = Bitmap.createScaledBitmap(mBitmap,
                (int) (dimensionScaled),
                (int) (dimensionScaled),
                false);

        Drawable mImageResized = new BitmapDrawable(getResources(), mBitmapResized);
        ((ImageView) findViewById(R.id.image)).setImageDrawable(mImageResized);

        if (mItem != null) {
            // Setting items' information
            ((TextView) findViewById(R.id.title)).setText(mItem.title);
            ((TextView) findViewById(R.id.quote)).setText(mItem.quote);
            ((TextView) findViewById(R.id.quality)).setText(mItem.quality);
            ((TextView) findViewById(R.id.description)).setText(mItem.description);
            ((TextView) findViewById(R.id.itemID)).setText(String.valueOf(mItem.ID));
            ((TextView) findViewById(R.id.type)).setText(mItem.type);
            String customLink = "<a href=\"" + mItem.link + "\">" + mItem.link + "</a>";

            ((TextView) findViewById(R.id.link)).setText(HtmlCompat.fromHtml(customLink, HtmlCompat.FROM_HTML_MODE_LEGACY));
            ((TextView) findViewById(R.id.link)).setClickable(true);
            ((TextView) findViewById(R.id.link)).setMovementMethod(LinkMovementMethod.getInstance());

            if (mItem.dps != null) {
                // Setting guns' specific information
                ((TextView) findViewById(R.id.credits))
                        .setText(getResources().getString(R.string.wiki_guns));

                ((TextView) findViewById(R.id.dps)).setText(mItem.dps);
                ((TextView) findViewById(R.id.mag_size)).setText(mItem.magSize);
                ((TextView) findViewById(R.id.ammo)).setText(mItem.ammo);
                ((TextView) findViewById(R.id.damage)).setText(mItem.damage);
                ((TextView) findViewById(R.id.fire_rate)).setText(mItem.fireRate);
                ((TextView) findViewById(R.id.reload)).setText(mItem.reload);
                ((TextView) findViewById(R.id.shot_speed)).setText(mItem.shotSpeed);
                ((TextView) findViewById(R.id.range)).setText(mItem.range);
                ((TextView) findViewById(R.id.force)).setText(mItem.force);
                ((TextView) findViewById(R.id.spread)).setText(mItem.spread);
            }
            else{
                // Setting items' (not guns) specific information
                ((TextView) findViewById(R.id.credits))
                        .setText(getResources().getString(R.string.wiki_items));

                findViewById(R.id.dps_layout).setVisibility(View.GONE);
                findViewById(R.id.mag_size_layout).setVisibility(View.GONE);
                findViewById(R.id.ammo_layout).setVisibility(View.GONE);
                findViewById(R.id.damage_layout).setVisibility(View.GONE);
                findViewById(R.id.fire_rate_layout).setVisibility(View.GONE);
                findViewById(R.id.reload_layout).setVisibility(View.GONE);
                findViewById(R.id.shot_speed_layout).setVisibility(View.GONE);
                findViewById(R.id.range_layout).setVisibility(View.GONE);
                findViewById(R.id.force_layout).setVisibility(View.GONE);
                findViewById(R.id.spread_layout).setVisibility(View.GONE);
            }

            try {
                // Adding synergies in item activity
                LinearLayout synLayout = (findViewById(R.id.synergy_layout));
                for (int indSyn = 0; indSyn < ApplicationModel.SYNERGIES.length(); indSyn++) {
                    JSONObject currSyn = ApplicationModel.SYNERGIES.getJSONObject(indSyn);
                    boolean found = false;

                    // Checking for each synergy if it contains the item considered
                    for (String idKey : ID_KEYS) {
                        JSONArray currIds = currSyn.getJSONArray(idKey);
                        for (int i = 0; i < currIds.length(); i++)
                            if (currIds.getInt(i) == mItem.ID)
                                found = true;
                    }


                    if (found) {
                        // Showing Fandom Wiki's link to synergies
                        findViewById(R.id.synergy_credits).setVisibility(View.VISIBLE);
                        findViewById(R.id.synergy_link).setVisibility(View.VISIBLE);

                        LinearLayout currSynLayout = new LinearLayout(this);
                        LinearLayout.LayoutParams synParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        currSynLayout.setOrientation(LinearLayout.VERTICAL);
                        synParams.setMargins(0, 30, 0, 30);

                        currSynLayout.setLayoutParams(synParams);
                        synLayout.addView(currSynLayout, synParams);

                        // Setting synergy's name
                        TextView synText = new TextView(this);
                        synText.setText(getString(R.string.item_synergy_name));
                        synText.setTypeface(synText.getTypeface(), Typeface.BOLD);
                        currSynLayout.addView(synText);

                        TextView synName = new TextView(this);
                        synName.setText(currSyn.getString("name"));
                        currSynLayout.addView(synName);

                        // Looking for IDs based on type of synergy items we are considering
                        for (int indKey = 0; indKey < ID_KEYS.length; indKey++) {
                            String idKey = ID_KEYS[indKey];
                            String nameKey = NAME_KEYS[indKey];
                            JSONArray currIds = currSyn.getJSONArray(idKey);
                            if (currIds.length() > 0) {

                                TextView mText = new TextView(this);
                                mText.setText(SYN_STRING[indKey]);
                                mText.setTypeface(mText.getTypeface(), Typeface.BOLD);
                                currSynLayout.addView(mText);

                                JSONArray currNames = currSyn.getJSONArray(nameKey);
                                // Showing items of the synergy
                                for (int indItem = 0; indItem < currIds.length(); indItem++) {
                                    ImageView itemImage = new ImageView(this);
                                    Drawable currImage = ApplicationModel.ITEMS.get(currIds.getInt(indItem) - 1).image;
                                    // Setting scaled image
                                    Bitmap currBitmap = ((BitmapDrawable)currImage).getBitmap();
                                    int synScale = displayMetrics.widthPixels / 6;

                                    Bitmap currBitmapResized = Bitmap.createScaledBitmap(currBitmap,
                                            (synScale),
                                            (synScale),
                                            false);

                                    Drawable currImageResized = new BitmapDrawable(getResources(), currBitmapResized);
                                    itemImage.setImageDrawable(currImageResized);
                                    LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    itemImage.setLayoutParams(imgLp);
                                    currSynLayout.addView(itemImage);

                                    TextView itemName = new TextView(this);
                                    itemName.setText(currNames.getString(indItem));
                                    currSynLayout.addView(itemName);

                                }

                            }
                        }

                        // Adding synergy description
                        TextView descText = new TextView(this);
                        descText.setText(getString(R.string.item_synergy_description));
                        descText.setTypeface(descText.getTypeface(), Typeface.BOLD);
                        currSynLayout.addView(descText);

                        TextView synDesc = new TextView(this);
                        synDesc.setText(currSyn.getString("description"));
                        currSynLayout.addView(synDesc);
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            // Setting toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.item_toolbar);
            toolbar.setTitle(mItem.title);
            toolbar.setSubtitle(mItem.quote);
            setSupportActionBar(toolbar);
        }
        ActionBar ab = getSupportActionBar();

        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Permits to simulate "back" button by clicking on the left arrow
     * (positioned at the top-left corner of the view).
     */
    public boolean onOptionsItemSelected(MenuItem myItem) {
        // Selecting the left arrow it will simulate "back" button
        if (myItem.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(myItem);
    }

}