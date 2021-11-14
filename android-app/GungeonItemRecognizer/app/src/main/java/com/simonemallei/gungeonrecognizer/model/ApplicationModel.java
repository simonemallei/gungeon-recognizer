package com.simonemallei.gungeonrecognizer.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.model.ApplicationModel
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ApplicationModel class containing the method that permits to load the items
 * and the synergies in the application.
 */
public class ApplicationModel {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = ApplicationModel.class.getName();
    /**
     * A List of ItemModel objects containing the ITEMS loaded by the database.
     */
    public static List<ItemModel> ITEMS = new ArrayList<ItemModel>();
    /**
     * A JSONArray containing the array of synergies.
     */
    public static JSONArray SYNERGIES;
    /**
     * A SQLiteDatabase object containing the database's reference.
     */
    public static SQLiteDatabase mDB = null;
    /**
     * A boolean that verifies whether the database is ready to be queried or not.
     */
    public static boolean isDbReady = false;
    /**
     * A String used for defining database's table name.
     */
    public static String TABLE_NAME = "ITEM";
    /**
     * A String used for log debugging.
     */
    private static DBMain dbManager = null;

    /**
     * Loads the model: hence the items (from the .sql database) and the
     * synergies (from the .json file).
     *
     * @param context Context object to get the assets.
     */
    public static void loadModel(final Context context) {

        // Loading synergies JSON
        Log.i(TAG_LOG, "Loading Synergies...");
        try {
            InputStream synStream = context.getAssets().open("db/synergies.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(synStream));
            StringBuilder synString = new StringBuilder();
            String currLine = "";
            while ((currLine = reader.readLine()) != null) {
                synString.append(currLine);
            }
            SYNERGIES = new JSONArray(synString.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG_LOG, "Synergies Loaded.");

        ITEMS.clear();
        // Loading Items from SQL Database
        Log.i(TAG_LOG, "Loading Items...");
        if (mDB != null)
        {
            Cursor result = mDB.rawQuery("SELECT * FROM " + TABLE_NAME +
                    " ORDER BY DBID", null);
            int idIndex = result.getColumnIndex("ID");
            int dbIDIndex = result.getColumnIndex("DBID");
            int titleIndex = result.getColumnIndex("TITLE");
            int quoteIndex = result.getColumnIndex("QUOTE");
            int qualityIndex = result.getColumnIndex("QUALITY");
            int descriptionIndex = result.getColumnIndex("DESCRIPTION");
            int typeIndex = result.getColumnIndex("ITEM_TYPE");
            int linkIndex = result.getColumnIndex("LINK");
            int dpsIndex = result.getColumnIndex("DPS");
            int magSizeIndex = result.getColumnIndex("MAG_SIZE");
            int ammoIndex = result.getColumnIndex("AMMO");
            int damageIndex = result.getColumnIndex("DAMAGE");
            int fireRateIndex = result.getColumnIndex("FIRE_RATE");
            int reloadIndex = result.getColumnIndex("RELOAD");
            int shotSpeedIndex = result.getColumnIndex("SHOT_SPEED");
            int rangeIndex = result.getColumnIndex("RANGE");
            int forceIndex = result.getColumnIndex("FORCE");
            int spreadIndex = result.getColumnIndex("SPREAD");

            while(result.moveToNext()) {
                int id = result.getInt(idIndex);
                int dbID = result.getInt(dbIDIndex);
                String title = result.getString(titleIndex);
                String quote = result.getString(quoteIndex);
                String quality = result.getString(qualityIndex);
                String description = result.getString(descriptionIndex);
                String type = result.getString(typeIndex);
                String link = result.getString(linkIndex);
                String dps = result.getString(dpsIndex);
                String magSize = result.getString(magSizeIndex);
                String ammo = result.getString(ammoIndex);
                String damage = result.getString(damageIndex);
                String fireRate = result.getString(fireRateIndex);
                String reload = result.getString(reloadIndex);
                String shotSpeed = result.getString(shotSpeedIndex);
                String range = result.getString(rangeIndex);
                String force = result.getString(forceIndex);
                String spread = result.getString(spreadIndex);

                try {
                    InputStream imageStream = context.getAssets().open("item_image/" + id + ".png");
                    Drawable image = Drawable.createFromStream(imageStream, null);

                    ItemModel item = new ItemModel(title, id, dbID, quote, quality, description,
                            image, type, link, dps, magSize, ammo, damage, fireRate, reload,
                            shotSpeed, range, force, spread);
                    ITEMS.add(item);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            result.close();
        }
        isDbReady = true;
        Log.i(TAG_LOG, "Items Loaded.");
    }

    /**
     * Init method that loads the items and the synergies from the asset's files.
     *
     */
    public static void init(final Context context) {
        dbManager = new DBMain(context);
        mDB = dbManager.loadDB();
    }

}
