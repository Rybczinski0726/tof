package com.example.tof;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

/*  This is an example of getting and processing ToF data.

    This example will only work (correctly) on a device with a front-facing depth camera
    with output in DEPTH16. The constants can be adjusted but are made assuming this
    is being run on a Samsung S10 5G device.
 */
public class MainActivity extends AppCompatActivity implements DepthFrameVisualizer {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int CAM_PERMISSIONS_REQUEST = 0;

    private static final int REQUEST_CAMERA_PERMISSIONS = 1;

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private TextureView rawDataView;
    private TextureView noiseReductionView;
    private TextureView movingAverageView;
    private TextureView blurredAverageView;
    private Matrix defaultBitmapTransform;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rawDataView = findViewById(R.id.rawData);
        noiseReductionView = findViewById(R.id.noiseReduction);
        movingAverageView = findViewById(R.id.movingAverage);
        blurredAverageView = findViewById(R.id.blurredAverage);

        checkCamPermissions();
        camera = new Camera(this, this);
        camera.openFrontDepthCamera();
    }

    private void checkCamPermissions() {
        if (!hasAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, CAM_PERMISSIONS_REQUEST);
        }
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onRawDataAvailable(Bitmap bitmap) {
        renderBitmapToTextureView(bitmap, rawDataView);
    }

    @Override
    public void onNoiseReductionAvailable(Bitmap bitmap) {
        renderBitmapToTextureView(bitmap, noiseReductionView);
    }

    @Override
    public void onMovingAverageAvailable(Bitmap bitmap) {
        renderBitmapToTextureView(bitmap, movingAverageView);
    }

    @Override
    public void onBlurredMovingAverageAvailable(Bitmap bitmap) {
        renderBitmapToTextureView(bitmap, blurredAverageView);
    }

    /* We don't want a direct camera preview since we want to get the frames of data directly
        from the camera and process.

        This takes a converted bitmap and renders it onto the surface, with a basic rotation
        applied.
     */
    private void renderBitmapToTextureView(Bitmap bitmap, TextureView textureView) {
        Canvas canvas = textureView.lockCanvas();
        canvas.drawBitmap(bitmap, defaultBitmapTransform(textureView), null);
        textureView.unlockCanvasAndPost(canvas);
    }

    private Matrix defaultBitmapTransform(TextureView view) {
        if (defaultBitmapTransform == null || view.getWidth() == 0 || view.getHeight() == 0) {
            Matrix matrix = new Matrix();
            int centerX = view.getWidth() / 2;
            int centerY = view.getHeight() / 2;

            int bufferWidth = DepthFrameAvailableListener.WIDTH;
            int bufferHeight = DepthFrameAvailableListener.HEIGHT;

            RectF bufferRect = new RectF(0, 0, bufferWidth, bufferHeight);
            RectF viewRect = new RectF(0, 0, view.getWidth(), view.getHeight());
            matrix.setRectToRect(bufferRect, viewRect, Matrix.ScaleToFit.CENTER);
            matrix.postRotate(270, centerX, centerY);

            defaultBitmapTransform = matrix;
        }
        return defaultBitmapTransform;
    }
}






