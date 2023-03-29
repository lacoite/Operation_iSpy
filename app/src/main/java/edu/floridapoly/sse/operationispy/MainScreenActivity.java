package edu.floridapoly.sse.operationispy;

//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.vision.v1.AnnotateImageRequest;
//import com.google.cloud.vision.v1.AnnotateImageResponse;
//import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
//import com.google.cloud.vision.v1.EntityAnnotation;
//import com.google.cloud.vision.v1.Feature;
//import com.google.cloud.vision.v1.Feature.Type;
//
//import com.google.cloud.vision.v1.Image;
//import com.google.cloud.vision.v1.ImageAnnotatorClient;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;

import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
//import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

//import io.grpc.netty.shaded.io.netty.util.internal.SystemPropertyUtil;


public class MainScreenActivity extends AppCompatActivity {

    private ObjectDetector objectDetector;
    private ImageLabeler imagelabeler;
    //private VisionImageProcessor imageProcessor;

    static Context context;

    static File finalFile = null;
    static Bitmap bitmap;
    static File tempFile = null;
    static Bitmap rotatedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();

        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "\"C:\\\\Users\\\\Latasha\\\\Downloads\\\\operation-ispy-9ed1efa38b6a.json\"");

        if(ContextCompat.checkSelfPermission(MainScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{
                    Manifest.permission.CAMERA}, 101);
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        Fragment homePromptFragment = new HomePromptFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView, homePromptFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


//        HomeImageDisplayFragment imageDisplayFragment = new HomeImageDisplayFragment();
//        fragmentTransaction.replace(R.id.fragmentContainerView, imageDisplayFragment);
//        //provide the fragment ID of your first fragment which you have given in
//        //fragment_layout_example.xml file in place of first argument
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();



    }

    public static File bitmapToFile(Context context,Bitmap bitmap) { // File name like "image.png"
        //create a file to write bitmap data
        try {
            tempFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
            //tempFile.createNewFile();

//Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return tempFile;
        }catch (Exception e){
            e.printStackTrace();
            return tempFile; // it will return null
        }
    }


    public static Context getContext(){
        return context;
    }

    public static File getFile(){
        //return finalFile;
        return finalFile;
    }
    public static Bitmap getBitmap(){
        //return finalFile;
        return rotatedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
            bitmap =  BitmapFactory.decodeFile("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
            rotateBitmap(bitmap);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            Fragment homeImageDisplayFragment = new HomeImageDisplayFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, homeImageDisplayFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void rotateBitmap(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try{
            exifInterface = new ExifInterface("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void submitToAPI() throws IOException{
        createCustomObjectDetectionImageProcessor();
        RunCustomObjectDetection();
        createLabelerImageProcessor();
        RunLabelerDetection();


        //finalFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
        //finalFile = bitmapToFile(getContext(), rotatedBitmap);
    }

    public void createCustomObjectDetectionImageProcessor(){
        Log.i("LOGGING: ", "Using Custom Object Detector Processor");
        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("custom_models/object_labeler.tflite")
                        .build();
        CustomObjectDetectorOptions customObjectDetectorOptions =
                new CustomObjectDetectorOptions.Builder(localModel)
                      .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                      .enableMultipleObjects()
                      .enableClassification()
                      .setMaxPerObjectLabelCount(10)
                      .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
    }

    public void createLabelerImageProcessor(){
        Log.i("LOGGING: ", "Using Custom Image label Detector Processor");
        ImageLabelerOptions imageLabelerOptions = new ImageLabelerOptions.Builder().build();
        imagelabeler = ImageLabeling.getClient(imageLabelerOptions);
    }

    private void RunCustomObjectDetection(){
        Log.i("LOGGING: ", "Try reload and detect image");
        if(objectDetector != null){
            objectDetector.process(rotatedBitmap, 0)
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                  @Override
                  public void onSuccess(List<DetectedObject> detectedObjects) {
                      if(detectedObjects.isEmpty() != true){
                          for (DetectedObject detectedObject : detectedObjects) {
                              for (DetectedObject.Label label : detectedObject.getLabels()) {
                                  String text = label.getText();
                                  //int index = label.getIndex();
                                  //float confidence = label.getConfidence();
                                  Log.i("OBJECT STRING", text);

                              }}
                      }else{
                          Log.i("LOGGING: ", "NO OBJECTS FOUND");
                      }

                    //imageProxy.close();
                  }
                });
        } else {
            Log.i("LOGGING: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
    }

    private void RunLabelerDetection(){
        Log.i("LOGGING: ", "Try reload and detect image2");
        if(imagelabeler != null){
            imagelabeler.process(rotatedBitmap, 0)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            for (ImageLabel label : labels) {
                                String text = label.getText();
                                //float confidence = label.getConfidence();
                                //int index = label.getIndex();
                                Log.i("LOGGING: ", "FROM LABEL = " + text);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("LOGGING: ", "NO OBJECTS FOUND");
                        }
                    });

        } else {
            Log.i("LOGGING: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
    }

    public void removeFragment(){
        Intent intent = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(intent, 101);

//        Fragment oldFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
//        if (oldFragment != null) {
//            getSupportFragmentManager().beginTransaction()
//                    .remove(oldFragment).commit();
//        }


        ////THIS CHUNK WORKS, IT REPLACES THE FRAGMENT
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//        HomeImageDisplayFragment homeImageFragment = new HomeImageDisplayFragment();
//        fragmentTransaction.replace(R.id.fragmentContainerView, homeImageFragment);
//        //provide the fragment ID of your first fragment which you have given in
//        //fragment_layout_example.xml file in place of first argument
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//



//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView));
//        //fragmentTransaction.remove(homePromptFragment).commit();
//        if(current != null){
//            Toast.makeText(getContext(), "Found Fragment", Toast.LENGTH_SHORT).show();
//            getSupportFragmentManager().beginTransaction().remove(current).commit();
//        }
//        else{
//            Toast.makeText(getContext(), "No Fragment", Toast.LENGTH_SHORT).show();
//
//        }

        //provide the fragment ID of your first fragment which you have given in
        //fragment_layout_example.xml file in place of first argument
        //fragmentTransaction.addToBackStack(null);
        //fragmentTransaction.commit();

    }
}