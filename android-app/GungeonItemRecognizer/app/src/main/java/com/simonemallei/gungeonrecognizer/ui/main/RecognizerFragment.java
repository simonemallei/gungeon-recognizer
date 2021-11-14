package com.simonemallei.gungeonrecognizer.ui.main;

import static com.simonemallei.gungeonrecognizer.MainActivity.CAMERA_REQ_CODE;

import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;

import com.simonemallei.gungeonrecognizer.MainActivity;
import com.simonemallei.gungeonrecognizer.R;
import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;
import com.simonemallei.gungeonrecognizer.adapter.ItemIconAdapter;
import com.simonemallei.gungeonrecognizer.listener.OnItemSelectedListener;
import com.simonemallei.gungeonrecognizer.model.ItemModel;
import com.simonemallei.gungeonrecognizer.thread.RecognizeThread;
import com.google.common.util.concurrent.ListenableFuture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.ui.main.RecognizerFragment
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * RecognizerFragment class containing the Recognition tab fragment.
 */
public class RecognizerFragment extends Fragment {

    /**
     * A String used for log debugging.
     */
    private static final String TAG_LOG = RecognizerFragment.class.getName();
    /**
     * An integer that contains the "what" value of an update message obtained by the UpdateHandler.
     */
    private final static int UPDATE_RESULT = 0;
    /**
     * An integer that codifies the recognition state:
     * - STATE_REC == -1 if the fragment has been started and has to start the camera.
     * - STATE_REC == 0 if there are no predictions to perform.
     * - STATE_REC == 1 if there is a prediction to perform and it has not been completed yet.
     */
    public int STATE_REC = -1;
    /**
     * An integer containing the number icons for each row (number of
     * columns in the recognition tab).
     */
    private final int NUM_ICONS = 5;
    /**
     * An integer containing the id of the "Take a photo!" String.
     */
    private final int PHOTO_TEXT = R.string.take_photo;
    /**
     * A double containing the thickness of the green square (in proportion
     * to the camera view's width).
     */
    private final double WIDTH_SQUARE = 0.025;
    /**
     * A double containing the minimum ratio for the size of the square (in proportion
     * to the camera view's width).
     */
    private final static double MIN_RATIO = 0.2;
    /**
     * A double containing the maximum ratio for the size of the square (in proportion
     * to the camera view's width).
     */
    private final static double MAX_RATIO = 0.95;
    /**
     * A double containing the chosen ratio for the size of the square (in proportion
     * to the camera view's width).
     */
    private double chosenRatio = MIN_RATIO;
    /**
     * A float containing the current linear zoom of the camera.
     */
    private static float mZoom = 0.0f;
    /**
     * A WeakReference of OnItemSelectedListener called when an item is selected by the user.
     */
    private WeakReference<OnItemSelectedListener> mOnItemSelectedListenerRef;
    /**
     * A pageViewModel reference to the ViewModel used.
     */
    private PageViewModel pageViewModel;
    /**
     * A List of ItemModel objects containing the recognition tab model.
     */
    public List<ItemModel> mModel;
    /**
     * A GeneralAdapter reference to the adapter used by the recognition tab (ItemIconAdapter).
     */
    private GeneralAdapter mAdapter;
    /**
     * A Context reference to the activity's context.
     */
    private Context mContext;
    /**
     * A View reference to the fragment's view.
     */
    private View root;
    /**
     * A PreviewView reference to the camera's SurfaceView.
     */
    private PreviewView previewView;
    /**
     * An UpdateHandler object to perform tab's model updates
     */
    private UpdateHandler mUpdateHandler;
    /**
     * A Camera reference to the camera used in previewView.
     */
    private Camera mCamera;
    /**
     * A ProcessCameraProvider to the camera's provider API.
     */
    private ProcessCameraProvider mProvider;
    /**
     * A ListenableFuture instance of the ProcessCameraProvider to the camera used.
     */
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    public RecognizerFragment() {
        super();
        mModel = new ArrayList<ItemModel>();
        mUpdateHandler = new UpdateHandler(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnItemSelectedListener) {
            final OnItemSelectedListener listener = (OnItemSelectedListener) context;
            mOnItemSelectedListenerRef = new WeakReference<OnItemSelectedListener>(listener);
        } else
            throw new IllegalStateException("Context must implement fragment's interface.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mOnItemSelectedListenerRef != null) {
            mOnItemSelectedListenerRef.clear();
            mOnItemSelectedListenerRef = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        pageViewModel.setIndex(index);
        cameraPermission();
    }

    /**
     * onStart method: sets the green square's SurfaceView in order to obtain the
     * expected performance (green square on top of the previewView).
     */
    @Override
    public void onStart() {
        super.onStart();

        // Setting SurfaceViews
        final SurfaceView mOverlyingSurface = root.findViewById(R.id.overlying_surface);
        mOverlyingSurface.setZOrderOnTop(true);
        SurfaceHolder mHolder = mOverlyingSurface.getHolder();
        mHolder.setFormat(PixelFormat.RGBA_8888);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Log.i("Overlying Surface", "loaded");
                drawSquare(mOverlyingSurface);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            }
        });

    }

    /**
     * When the fragment is resumed, restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    /**
     * When the fragment is paused, sets the state of the camera as not started (STATE_REC != 0).
     */
    @Override
    public void onPause() {
        super.onPause();
        STATE_REC = -1;
    }

    /**
     * onCreateView() method: sets GeneralAdapter for the model, defines onClick()
     * method for the "Take the photo!" button and zoom/green square's size seekbars
     * onProgressChanged methods.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.i("Current STATE_REC", Integer.toString(STATE_REC));

        // Setting Recognizer Tab layout
        root = inflater.inflate(R.layout.recognizer_tab, container, false);

        final GridView mGridView = root.findViewById(R.id.result_grid);

        mGridView.setNumColumns(NUM_ICONS);

        // Setting ItemIconAdapter
        mAdapter = getCustomAdapter(container);
        mGridView.setAdapter(mAdapter);

        final OnItemSelectedListener listener = mOnItemSelectedListenerRef.get();
        // Setting the click listener
        mGridView.setOnItemClickListener(listener.getItemListener(mAdapter));

        final Button mButton = root.findViewById(R.id.change_state);
        mButton.setText(getContext().getResources().getString(PHOTO_TEXT));
        final RecognizerFragment mFrag = this;
        // Setting the button's click listener
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Button mButton = root.findViewById(R.id.change_state);
                if (STATE_REC == 0) {
                    // Performing item recognition if the camera is granted
                    if (isCameraGranted()) {
                        STATE_REC = 1;
                        previewView = root.findViewById(R.id.camera);
                        Bitmap mBitmap = previewView.getBitmap();
                        int startPixel = (int) ((mBitmap.getWidth() - mBitmap.getWidth() * chosenRatio) / 2);
                        int edgeLength = (int) (mBitmap.getWidth() * chosenRatio);
                        Log.i("Chosen Ratio", String.valueOf(chosenRatio));
                        Log.i("Start Pixel", String.valueOf(startPixel));
                        Log.i("Edge Length", String.valueOf(edgeLength));
                        mBitmap = Bitmap.createBitmap(mBitmap, startPixel, startPixel, edgeLength, edgeLength);

                        new RecognizeThread(mFrag, mBitmap, NUM_ICONS).start();

                        final GridView mGridView = root.findViewById(R.id.result_grid);
                        final OnItemSelectedListener listener = mOnItemSelectedListenerRef.get();
                        mGridView.setOnItemClickListener(listener.getItemListener(mAdapter));
                        mProvider.unbindAll();
                    }
                }
            }
        });

        // Setting Square SeekBar
        SeekBar mSeekBar = root.findViewById(R.id.thumb);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                double currProgress = (double) seekBar.getProgress();
                double dimension = MIN_RATIO + (double) (currProgress / seekBar.getMax())
                        * (MAX_RATIO - MIN_RATIO);
                Log.i("Square's Seekbar's Progress", String.valueOf(currProgress));
                Log.i("Square's Seekbar's new Progress", String.valueOf(i));
                Log.i("Square's SeekBar's Dimension", String.valueOf(dimension));
                chosenRatio = dimension;
                drawSquare((SurfaceView) (root.findViewById(R.id.overlying_surface)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setting Zoom SeekBar
        SeekBar mZoomBar = root.findViewById(R.id.zoom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mZoomBar.setProgress(mZoomBar.getMin());
        else
            mZoomBar.setProgress(0);
        mZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mZoom = getZoomValue(seekBar);
                if (STATE_REC == 0) {
                    // Changing zoom if the camera is running
                    changeZoom();
                    Log.i("Seekbar's new Progress", String.valueOf(i));
                    Log.i("Zoom Linear Value:", "" + mZoom + "x");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return root;
    }

    /**
     * Gets zoom value in range [0; 1] based on zoom's SeekBar value.
     *
     * @param seekBar zoom's SeekBar used by the fragment.
     * @return A float containing the zoom value in range [0; 1].
     */
    private float getZoomValue(SeekBar seekBar) {
        // Getting zoom value based on the seekbar value
        float zoom = 1.0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            zoom = ((float) (seekBar.getProgress() - seekBar.getMin()) /
                    (float) (seekBar.getMax() - seekBar.getMin()));
        else
            zoom = ((float) (seekBar.getProgress()) / (float) (seekBar.getMax()));
        return zoom;
    }

    /**
     * Changes the zoom in the previewView based on the internal mZoom value.
     */
    private void changeZoom() {
        // Changing zoom if the permission is granted
        if (isCameraGranted()) {
            CameraControl mControl = mCamera.getCameraControl();
            mControl.setLinearZoom(mZoom);
        }
    }

    /**
     * If the camera permission is not granted, asks the user for the permission.
     */
    private void cameraPermission() {
        MainActivity mActivity = (MainActivity) getContext();
        // Asking for camera permission if the camera is not granted
        if (!isCameraGranted()) {
            AlertDialog mDialog = new AlertDialog.Builder(getContext()).setTitle("Camera Permission")
                    .setMessage(getContext().getResources().getString(R.string.permission_msg))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, CAMERA_REQ_CODE);
                        }
                    }).create();
            mDialog.show();

        }
    }

    /**
     * Starts the camera if its permission is granted.
     */
    private void startCamera(){
        Log.i(TAG_LOG, "Starting camera...");
        // Starting camera if the permission is granted
        if (isCameraGranted()) {
            previewView = root.findViewById(R.id.camera);
            previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
            bindImagePreview();
            STATE_REC = 0; // Now the camera has been started with succeed.
        }
        else
            Log.i(TAG_LOG, "Camera's permission not granted.");
    }

    /**
     * Binds the camera used to the fragment's context.
     */
    private void bindImagePreview() {
        // Adding listener on cameraProviderFuture
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    /**
     * Configures the camera based on zoom's SeekBar and other needs
     * in order to obtain all the functionalities needed.
     */
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        // Adding camera settings
        CameraController mController = new LifecycleCameraController(getContext());
        mController.setTapToFocusEnabled(true);
        mController.setPinchToZoomEnabled(false);

        previewView.setController(mController);

        // Setting Exposure Compensation
        Preview.Builder builder = new Preview.Builder();
        Camera2Interop.Extender<Preview> extender = new Camera2Interop.Extender<>(builder);
        extender.setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -1);

        Preview preview = builder.build();

        // Setting last information about the camera used
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        mCamera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
        CameraControl mControl = mCamera.getCameraControl();
        mControl.setLinearZoom(mZoom);
        mProvider = cameraProvider;

    }

    /**
     * Draws the green square in the parameter based on fragment's chosenRatio and the WIDTH_SQUARE.
     *
     * @param mOverlyingSurface SurfaceView that will contain the green square.
     */
    private void drawSquare(SurfaceView mOverlyingSurface) {
        // Drawing the square based on the chosenRatio
        SurfaceHolder mHolder = mOverlyingSurface.getHolder();
        try {
            Canvas mCanvas = mHolder.lockCanvas();

            Paint mPaint = new Paint();
            int widthSurface = mOverlyingSurface.getMeasuredWidth();
            // Clearing Surface View
            mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            mPaint.setAlpha(255);
            mPaint.setColor(0xff00ff00);
            int startPixel = (int) (widthSurface * (1 - (chosenRatio + WIDTH_SQUARE)) / 2);
            int edgeLength = (int) (widthSurface * (chosenRatio + WIDTH_SQUARE));
            int endPixel = startPixel + edgeLength;
            mPaint.setStyle(Paint.Style.STROKE);

            mPaint.setStrokeWidth((float) (widthSurface * (WIDTH_SQUARE / 2)));
            // Drawing new square
            mCanvas.drawRect(startPixel, startPixel, endPixel, endPixel, mPaint);

            mHolder.unlockCanvasAndPost(mCanvas);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("EXCEPTION", e.getClass().toString());

        }
    }

    /**
     * Verifies whether the camera permission is granted or not.
     *
     * @return True if the permission is granted.
     */
    private boolean isCameraGranted() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Creates an ItemIconAdapter instance for the fragment.
     *
     * @param container ViewGroup reference to the fragment.
     * @return The fragment's ItemIconAdapter.
     */
    private GeneralAdapter getCustomAdapter(ViewGroup container) {
        return new ItemIconAdapter(mModel, this, 5, 5 *2, container);
    }

    /**
     * Performs steps to show the neural network's prediction to the user.
     */
    private void showPrediction() {
        // Setting camera again
        startCamera();
        // Updating adapter model
        mAdapter.setModel(mModel);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Method that sends the message to the UpdateHandler, asking for an update.
     */
    public void updateResult() {
        final Message updateMessage = mUpdateHandler.obtainMessage(UPDATE_RESULT);
        mUpdateHandler.sendMessage(updateMessage);
    }

    private static class UpdateHandler extends Handler {
        private WeakReference<RecognizerFragment> mFragRef;

        public UpdateHandler(final RecognizerFragment mFrag) {
            this.mFragRef = new WeakReference<>(mFrag);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_RESULT) {
                final RecognizerFragment mFrag = mFragRef.get();
                if (mFrag != null)
                    mFrag.showPrediction();
            }
        }
    }
}
