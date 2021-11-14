package com.simonemallei.gungeonrecognizer.model;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.model.DBMain
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * DBMain class containing the methods that create the SQLiteDatabase object.
 */
public class DBMain extends SQLiteOpenHelper {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = DBMain.class.getName();
    /**
     * A String containing the database's path.
     */
    private static final String DB_PATH= "data/data/com.simonemallei.gungeonrecognizer/databases/";
    /**
     * A String containing the database's name.
     */
    private static final String DB_NAME = "gungeon_db";
    /**
     * A SQLiteDatabase object that represents the database used by the application.
     */
    private SQLiteDatabase dbObj = null;
    /**
     * A Context reference to get asset's file.
     */
    private final Context context;
    /**
     * An integer containing the database's version.
     */
    private static final int DATABASE_VERSION = 8;

    public DBMain(Context context) {
        super(context,  DB_NAME, null, DATABASE_VERSION);
        this.context  = context;
    }

    /**
     * Creates the database reference.
     */
    public void createDB() throws IOException {
        this.getReadableDatabase();
        try {
            copyDB();

        } catch (IOException e) {

            throw new Error("Error copying database");
        }
    }

    /**
     * Copies the database from the asset's file.
     */
    public void copyDB() throws IOException{
        try {
            // Copying database from the assets file
            InputStream in =  context.getAssets().open("db/"+DB_NAME+".db");
            String outPath =  DB_PATH  +  DB_NAME + ".db";
            OutputStream out = new FileOutputStream(outPath);
            byte[] bufferBytes = new byte[1024];
            int length;
            while ((length = in.read(bufferBytes))>0){
                out.write(bufferBytes, 0, length);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens database from its path.
     */
    public void openDB() throws SQLException {
        // Opening SQL Database
        String myPath = DB_PATH + DB_NAME + ".db";
        dbObj = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Loads database (creating database's file and opening it).
     */
    public SQLiteDatabase loadDB() {
        try {
            createDB();
            openDB();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dbObj;
    }

    @Override
    public synchronized void close() {
        if(dbObj != null)
            dbObj.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            Log.i(TAG_LOG, "Database to update");
        else
            Log.i(TAG_LOG, "Database already updated");
    }
}