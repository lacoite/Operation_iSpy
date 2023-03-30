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

import com.google.android.gms.tasks.OnCompleteListener;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import java.util.Locale;
import java.util.Map;

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
    //will need to be set based on a value in the firebase db
    public static int submissionAccepted;
    static String prompt = "phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();

        if (ContextCompat.checkSelfPermission(MainScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{
                    Manifest.permission.CAMERA}, 101);
        }

        //initialize firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("Prompts").document("1");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("YES", "DocumentSnapshot data: " + document.getData());
                        prompt = document.getData().get("name").toString();
                        //prompt = document.getData().get("name");
                        Log.d("The prompt is: ", prompt);
                    } else {
                        Log.d("WHERE", "No such document");
                    }
                } else {
                    Log.d("NO", "get failed with ", task.getException());
                }
            }
        });

        //listener snapshot
        final DocumentReference docRefDaily = db.collection("DailyPrompt").document("DateAndPrompt");
        docRefDaily.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("NO", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("YES", "Current data: " + snapshot.getData());
                } else {
                    Log.d("NULL", "Current data: null");
                }
            }
        });

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

    public void updateSubmission(){
        submissionAccepted = 100;
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
        RunCustomObjectDetection();
    }

    public void createLabelerImageProcessor(){
        Log.i("LOGGING: ", "Using Custom Image label Detector Processor");
        ImageLabelerOptions imageLabelerOptions = new ImageLabelerOptions.Builder().build();
        imagelabeler = ImageLabeling.getClient(imageLabelerOptions);
        RunLabelerDetection();
    }

    public void RunCustomObjectDetection(){
        Log.i("LOGGING: ", "Try reload and detect image");
        if(objectDetector != null){
            objectDetector.process(rotatedBitmap, 0)
                    .addOnCompleteListener(new OnCompleteListener<List<DetectedObject>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<DetectedObject>> task) {
                                createLabelerImageProcessor();
                        }
                    })
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                  @Override
                  public void onSuccess(List<DetectedObject> detectedObjects) {
                      if(detectedObjects.isEmpty() != true){
                          for (DetectedObject detectedObject : detectedObjects) {
                              for (DetectedObject.Label label : detectedObject.getLabels()) {
                                  String text = label.getText().toLowerCase(Locale.ROOT);
                                  //int index = label.getIndex();
                                  //float confidence = label.getConfidence();
                                  Log.i("OBJECT STRING", text);
                                  if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                      Log.i("FOUND!!!", "IN OD");
                                      updateSubmission();
                                  }
                              }}
                      }else{
                          Log.i("LOGGING: ", "NO OBJECTS FOUND");
                      }
                  }
                });
        } else {
            Log.i("LOGGING: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
    }

    public void RunLabelerDetection(){
        Log.i("LOGGING: ", "Try reload and detect image2");
        if(imagelabeler != null){
            imagelabeler.process(rotatedBitmap, 0)
                    .addOnCompleteListener(new OnCompleteListener<List<ImageLabel>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<ImageLabel>> task) {
                            if(submissionAccepted == 100){
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager
                                        .beginTransaction();
                                Fragment homeAnalysisTCCFragment = new HomeAnalysisTCCFragment();
                                fragmentTransaction.replace(R.id.fragmentContainerView, homeAnalysisTCCFragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                            else{
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager
                                        .beginTransaction();
                                Fragment homeAnalysisTNIFragment = new HomeAnalysisTNIFragment();
                                fragmentTransaction.replace(R.id.fragmentContainerView, homeAnalysisTNIFragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            for (ImageLabel label : labels) {
                                String text = label.getText().toLowerCase(Locale.ROOT);
                                //float confidence = label.getConfidence();
                                //int index = label.getIndex();
                                Log.i("LOGGING: ", "FROM LABEL = " + text);
                                if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                    Log.i("FOUND!!!", "IN LABELER");
                                    //submissionAccepted = 100;
                                    updateSubmission();
                                }
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
        Log.i("ABOUT TO CHECK", " SUB");
    }

    public void submitToAPI(){
//        finalFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
//        finalFile = bitmapToFile(getContext(), rotatedBitmap);
        submissionAccepted = 0;
        createCustomObjectDetectionImageProcessor();
        //RunCustomObjectDetection();
       // createLabelerImageProcessor();
        //RunLabelerDetection();
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