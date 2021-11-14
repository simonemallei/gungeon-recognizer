package com.simonemallei.gungeonrecognizer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.simonemallei.gungeonrecognizer.model.ApplicationModel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Random;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.SplashActivity
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * Splash Activity class containing application initial steps.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = SplashActivity.class.getName();
    /**
     * A long containing the minimum waiting interval in milliseconds.
     */
    private static final long MIN_WAIT_INTERVAL = 1500L;
    /**
     * A long containing the maximum waiting interval in milliseconds.
     */
    private static final long MAX_WAIT_INTERVAL = 5000L;
    /**
     * An integer containing the code for goAhead() method.
     */
    private static final int GO_AHEAD_WHAT = 1;
    /**
     * An integer containing the number of database's items.
     */
    private static final int NUM_ITEMS = 509;
    /**
     * A String containing the IS_DONE_KEY.
     */
    private static final String IS_DONE_KEY = "gungeonrecognizer.key.IS_DONE_KEY";
    /**
     * A String containing the START_TIME_KEY.
     */
    private static final String START_TIME_KEY = "gungeonrecognizer.key.START_TIME_KEY";
    /**
     * A long containing the starting time of the activity in milliseconds.
     */
    private long mStartTime = -1L;
    /**
     * A boolean that verifies if the goAhead() method is already called.
     */
    private boolean mIsDone;
    /**
     * A UiHandler containing the activity's message Handler.
     */
    private UiHandler mHandler;
    private static class UiHandler extends Handler {
        private final WeakReference<SplashActivity> mActivityRef;



        public UiHandler(final SplashActivity srcActivity){
            this.mActivityRef = new WeakReference<>(srcActivity);
        }

        /**
         * Method that, based on msg value, could start MainActivity.
         *
         * @param msg Message containing the value based on what the handler needs to perform.
         */
        @Override
        public void handleMessage(Message msg) {
            final SplashActivity srcActivity = this.mActivityRef.get();
            if (srcActivity == null) {
                Log.d(TAG_LOG, "Reference to SplashActivity lost!");
                return;
            }
            if (msg.what == GO_AHEAD_WHAT) {
                // If the message has been sent and MainActivity has not been already started
                long elapsedTime = SystemClock.uptimeMillis() - srcActivity.mStartTime;
                if (elapsedTime >= MIN_WAIT_INTERVAL && !srcActivity.mIsDone) {
                    srcActivity.mIsDone = true;
                    srcActivity.goAhead();
                }
            }
            else {
                throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    }

    /**
     * Method called during activity's creation: sets the Splash Image's Touch Listener
     * and loads database.
     *
     * @param savedInstanceState a Bundle object that contains the saved activity's state.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forcing Portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Setting layout
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        // Recovering startTime value from savedInstanceState
        if (savedInstanceState != null){
            this.mStartTime = savedInstanceState.getLong(START_TIME_KEY);
        }

        // Setting the Splash Image's Click Listener.
        mHandler = new UiHandler(this);
        final ImageView splashImageView = (ImageView) findViewById(R.id.splash_imageview);

        // Loading a random item as Splash Image
        int random_item = (new Random()).nextInt(NUM_ITEMS) + 1;
        InputStream imageStream = null;
        try {
            // Reading the image
            imageStream = getAssets().open("item_image/" + random_item + ".png");
            Drawable mImage = Drawable.createFromStream(imageStream, null);

            // Scaling the splash image based on the display
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int dimScaled = displayMetrics.widthPixels;
            Bitmap mBitmap = ((BitmapDrawable)mImage).getBitmap();
            Bitmap mBitmapResized = Bitmap.createScaledBitmap(mBitmap, dimScaled, dimScaled, false);
            Drawable mImageResized = new BitmapDrawable(getResources(), mBitmapResized);
            splashImageView.setImageDrawable(mImageResized);
        } catch (IOException e) {
            e.printStackTrace();
        }


        splashImageView.setOnClickListener(v -> {
            long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
            // If splash image is clicked after minimum waiting interval, MainActivity is started
            if(elapsedTime >= MIN_WAIT_INTERVAL && !mIsDone){
                mIsDone = true;
                goAhead();
            }
            else {
                Log.d(TAG_LOG, "Too early!");
            }
        });

        // Loading the items database
        ApplicationModel.init(this);
        ApplicationModel.loadModel(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mIsDone = savedInstanceState.getBoolean(IS_DONE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_DONE_KEY, mIsDone);
        outState.putLong(START_TIME_KEY, mStartTime);
    }

    /**
     * onStart() method: sends message to UiHandler after maximum waiting interval.
     */
    @Override
    protected void onStart(){
        super.onStart();
        if (mStartTime == -1L)
            mStartTime = SystemClock.uptimeMillis();
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        // Starting Main Activity after maximum waiting interval
        mHandler.sendMessageAtTime(goAheadMessage, mStartTime + MAX_WAIT_INTERVAL);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Starts MainActivity.
     */
    private void goAhead() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}