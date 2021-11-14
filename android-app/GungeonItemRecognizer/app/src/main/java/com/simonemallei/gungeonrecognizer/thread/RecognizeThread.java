package com.simonemallei.gungeonrecognizer.thread;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.simonemallei.gungeonrecognizer.model.ApplicationModel;
import com.simonemallei.gungeonrecognizer.model.ItemModel;
import com.simonemallei.gungeonrecognizer.ui.main.RecognizerFragment;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.thread.RecognizeThread
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * RecognizeThread class containing steps to recognition items in a photo.
 */
public class RecognizeThread implements Runnable {

    /**
     * A boolean that verifies if the thread is running.
     */
    private volatile boolean mRunning;
    /**
     * A WeakReference of RecognizerFragment object in order to update
     * the prediction in the recognition tab.
     */
    private WeakReference<RecognizerFragment> mFragRef;
    /**
     * An integer containing the scaled width of the image considered.
     */
    private final static int WIDTH_VAL=32;
    /**
     * An integer containing the scaled height of the image considered.
     */
    private final static int HEIGHT_VAL=32;
    /**
     * An integer containing the number of the channels of the image.
     */
    private final static int NUM_CHANNELS=3;
    /**
     * An integer containing the number of each prediction
     * for each row (number of columns in the recognition tab).
     */
    private int NUM_ITEMS;
    /**
     * An integer containing the number of item rows in the recognition tab.
     */
    private final static int NUM_ROWS = 2;
    /**
     * A String containing the path to the recognition model.
     */
    private final static String FILE_PATH="recognizer_model/";
    /**
     * An integer containing the number of items
     * (hence the size of the output array obtained by the neural network).
     */
    private final static int ITEMS_NUM = 509;
    /**
     * An integer containing the (x,y) value (x == y) of the top-left
     * pixel in the image.
     */
    private int startPixel;
    /**
     * An integer containing the length of the image's edge (height and width).
     */
    private int edgeLength;
    /**
     * A Thread object referring to the thread called by the class.
     */
    private Thread mThread;
    /**
     * A Bitmap object containing the image during the prediction process.
     */
    private Bitmap mBitmap = null;

    public RecognizeThread(RecognizerFragment mFrag, Bitmap mBitmap, int num_cols){
        this.mFragRef = new WeakReference<>(mFrag);
        this.mBitmap = mBitmap;
        this.startPixel = 0;
        this.edgeLength = mBitmap.getWidth();
        NUM_ITEMS = num_cols * NUM_ROWS;
    }

    public void start() {
        if (!mRunning) {
            mRunning = true;
            mThread = new Thread(this);
            mThread.start();
        }
    }

    /**
     * Performs histEqualization of a HSV image (with size width * height)
     * on V values.
     *
     * @param hsvColor array of HSV values in range [0; 1] where:
     *                 - hsvColor[x][y][0] contains H value on (x, y) pixel,
     *                 - hsvColor[x][y][1] contains S value on (x, y) pixel,
     *                 - hsvColor[x][y][2] contains V value on (x, y) pixel.
     * @param width size of first dimension of hsvColor (x-axis).
     * @param height size of second dimension of hsvColor (y-axis).
     */
    private void histEqualization(float[][][] hsvColor, int width, int height){
        // Performing hist equalization on V values (from HSV image)
        float [] vValues = new float[width * height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                    vValues[x*height + y] = hsvColor[x][y][2];
        Arrays.sort(vValues);

        // Finding minimum cumulative distribution function
        int min_cdf = 1;
        while (min_cdf != width * height && vValues[min_cdf] == vValues[min_cdf-1])
            min_cdf++;

        // Finding cumulative distribution for each pixel and converting it into
        // new V value for the resulting HSV image
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                int start = 1;
                int end = width * height;
                int pix_cdf = width * height;
                while (start <= end){
                    int medium = (start + end) / 2;
                    if (vValues[medium-1] > hsvColor[x][y][2]){
                        pix_cdf = medium;
                        end = medium - 1;
                    }
                    else
                        start = medium + 1;
                }
                hsvColor[x][y][2] = ((float) (pix_cdf - min_cdf)) / ((float) (width * height - min_cdf));
            }
        }
    }

    /**
     * Performs histogram equalization on V values, then it scales the bitmap obtained
     * to a 32x32 image.
     * Afterwards the neural networks predicts the items in the image and eventually the
     * thread updates recognition tab's model.
     */
    public void run() {
        RecognizerFragment mFrag = mFragRef.get();

        List<ItemModel> newModel = new ArrayList<>();
        if (mBitmap != null) {

            // Mapping RGB to HSV
            float[][][] hsvColor = new float[mBitmap.getWidth()][mBitmap.getHeight()][NUM_CHANNELS];
            for (int x = 0; x < mBitmap.getWidth(); x++) {
                for (int y = 0; y < mBitmap.getHeight(); y++) {
                    int mPixel = mBitmap.getPixel(x, y);
                    int[] pixelColor = new int[NUM_CHANNELS];
                    pixelColor[0] = (mPixel >> 16) & 0xff;
                    pixelColor[1] = (mPixel >> 8) & 0xff;
                    pixelColor[2] = mPixel & 0xff;
                    Color.RGBToHSV(pixelColor[0], pixelColor[1], pixelColor[2], hsvColor[x][y]);
                }
            }
            // Applying Histogram Equalization on V
            histEqualization(hsvColor, mBitmap.getWidth(), mBitmap.getHeight());
            // Mapping HSV to RGB
            int[] newBitmap = new int[mBitmap.getWidth() * mBitmap.getHeight()];
            for (int x = 0; x < mBitmap.getWidth(); x++)
                for (int y = 0; y < mBitmap.getHeight(); y++){
                    newBitmap[y * mBitmap.getHeight() + x] = Color.HSVToColor(hsvColor[x][y]);
                }
            mBitmap = Bitmap.createBitmap(newBitmap, mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            mBitmap = Bitmap.createBitmap(mBitmap, startPixel, startPixel, edgeLength, edgeLength);

            // Scaling bitmap to *WIDTH_VAL*, *HEIGHT_VAL*
            mBitmap = Bitmap.createScaledBitmap(mBitmap, WIDTH_VAL, HEIGHT_VAL, true);

            AssetFileDescriptor mDescriptor = null;
            try {
                // Loading TensorFlow Lite model
                String modelPath = FILE_PATH + "GungeonModel.tflite";
                mDescriptor = mFrag.getContext().getAssets().openFd(modelPath);
                FileInputStream mStream = new FileInputStream(mDescriptor.getFileDescriptor());
                FileChannel mChannel = mStream.getChannel();
                MappedByteBuffer mBuffer = mChannel.map(FileChannel.MapMode.READ_ONLY,
                        mDescriptor.getStartOffset(), mDescriptor.getDeclaredLength());
                Interpreter mInterpreter = new Interpreter((ByteBuffer)(mBuffer));
                float [][][][] mInput = new float[1][WIDTH_VAL][HEIGHT_VAL][NUM_CHANNELS];
                float [][] mOutput = new float[1][ITEMS_NUM];
                // From RGB [0-255] range, to RGB [0-1] range
                // Y is the row
                // X is the column
                for (int x = 0; x < WIDTH_VAL; x++)
                    for (int y = 0; y < HEIGHT_VAL; y++){
                        int mPixel = mBitmap.getPixel(x, y);
                        float [] pixelColor = new float[NUM_CHANNELS];
                        pixelColor[0] = (mPixel >> 16) & 0xff;
                        pixelColor[1] = (mPixel >> 8) & 0xff;
                        pixelColor[2] = mPixel & 0xff;
                        for (int col = 0; col < pixelColor.length; col++)
                            mInput[0][y][x][col] = pixelColor[col] / 255.0f;
                    }

                // Model prediction
                mInterpreter.run(mInput, mOutput);

                // Finding *NUM_ITEMS* result items and adding them into the new model
                int [] max_ind = new int[NUM_ITEMS];
                Arrays.fill(max_ind, -1);
                for (int i = 0; i < mOutput[0].length; i++) {
                    int j = 0;
                    while(j < max_ind.length && max_ind[j] != -1 && mOutput[0][i] < mOutput[0][max_ind[j]])
                        j++;
                    if (j != max_ind.length) {
                        if (max_ind[j] == -1)
                            max_ind[j] = i;
                        else {
                            for (int k = max_ind.length-1; k > j; k--)
                                max_ind[k] = max_ind[k-1];
                            max_ind[j] = i;
                        }
                    }
                }
                for (int i = 0; i < max_ind.length; i++)
                    newModel.add(ApplicationModel.ITEMS.get(max_ind[i]));

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        mFrag.mModel.clear();
        mFrag.mModel = newModel;
        mFrag.updateResult();
    }
}
