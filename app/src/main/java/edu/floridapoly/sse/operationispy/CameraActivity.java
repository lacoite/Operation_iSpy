package edu.floridapoly.sse.operationispy;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    //Declare request code permisisons variable
    private int REQUEST_CODE_PERMISSIONS = 1001;
    //Declare permissions necessary for camera
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    //Declare PreviewView, button, and ImageCapture
    PreviewView mPreviewView;
    Button captureImageButton;
    ImageCapture imageCapture;

    //Declare file and context
    File file;
    Context context;

    //When the activity is launched, lock the orientation in portrait, request permission if they have not been granted, then start camera
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //When back is pressed, close the activity and return the code for no image captured
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                setResult(202, resultIntent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        mPreviewView = findViewById(R.id.previewView);
        captureImageButton = findViewById(R.id.imageCaptureButton);
        context = this;

        //If all permissions are granted, start the camera. Otherwise request permissions defined in REQUIRED_PERMISSIONS array
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS); //request permissions if they have not been granted
        }
    }

    //Starts camera
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(MainScreenActivity.getContext());

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(MainScreenActivity.getContext()));
    }

    //Builds camera preview and sets onClick listener for capture button
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();

        imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

        //When the capture button is clicked, call capture image function
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePic();
            }
        });
    }

    //Capture and save the image to the cache directory under the name "currentImage.jpg"
    private void capturePic(){
        //Initialize file with cache file path
        file = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");

        //Capture image and save to the cache directory close activity with code for image captured, if failed, print exception StackTrace
        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(MainScreenActivity.getContext()), new ImageCapture.OnImageSavedCallback(){
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent resultIntent = new Intent();
                setResult(101, resultIntent);
                finish();
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    //Check if all desired permissions are granted
    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    //If user accepts permissions, launch camera, otherwise, ask user to update device settings and close camera. Camera will not open until the permisison is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Please Allow Camera Access In Your Device Settings", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

}