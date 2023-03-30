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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;

import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainScreenActivity extends AppCompatActivity {

    static Context context;

    private ObjectDetector objectDetector;
    private ImageLabeler imagelabeler;

    static File finalFile = null;
    static Bitmap tempBitmap;
    static File tempFile = null;
    static Bitmap finalBitmap;

    //will need to be set based on a value in the firebase db
    public static int submissionAccepted;
    static String prompt;

    //Fragments
    Fragment homeHeaderFragment;
    Fragment homePromptFragment;
    Fragment homeImageDisplayFragment;
    Fragment homeAnalysisTCCFragment;
    //combine with IC fragment format since it is no longer needed
    Fragment homeAnalysisTNIFragment;
    Fragment homeTargetSpyedFragment;


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

        //Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRefForPrompt = db.collection("Prompts").document("1");
        docRefForPrompt.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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


        fragmentSwap(1);
    }

    //Creates updates the image file with the finalBitmap
    public static File bitmapToFile(Context context,Bitmap bitmap) {
        //Create a file to write bitmap data
        try {
            tempFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            //Write the bytes in file
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


    //Returns application's context
    public static Context getContext(){
        return context;
    }

    //Returns finalFile
    public static File getFile(){
        return finalFile;
    }

    //Returns corrected bitmap
    public static Bitmap getBitmap(){
        return finalBitmap;
    }


    //When the Camera Activity ends, update the tempBitmap and finalBitmap and switch to the image display fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
            tempBitmap =  BitmapFactory.decodeFile("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
            rotateBitmap(tempBitmap);
            fragmentSwap(2);
        }
    }

    //Creates a new bitmap, finalBitmap, with corrected orientation
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
        finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //Creates and runs Custom Object Detector image processor from ML Kit
    public void createCustomObjectDetectionImageProcessor(){
        Log.i("LOGGING: ", "Using Custom Object Detector Processor");
        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("custom_models/object_labeler.tflite")
                        .build();
        //Allow detection of multiple objects, enables classifications, allows 10 labels
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

    //Creates and runs Labeler image processor from ML Kit
    public void createLabelerImageProcessor(){
        Log.i("LOGGING: ", "Using Custom Image label Detector Processor");
        ImageLabelerOptions imageLabelerOptions = new ImageLabelerOptions.Builder().build();
        imagelabeler = ImageLabeling.getClient(imageLabelerOptions);
        RunLabelerDetection();
    }

    //If run the Object Detection processor on the finalBitmap, then call method to create and run Labeler processor
    public void RunCustomObjectDetection(){
        InputImage image = InputImage.fromBitmap(finalBitmap, 0);
        if(objectDetector != null){
            objectDetector.process(image)
                    //If process is successful, compare the objects to the prompt. If the prompt is found, update the submissionAccepted variable
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                  @Override
                  public void onSuccess(List<DetectedObject> detectedObjects) {
                      if(detectedObjects.isEmpty() != true){
                          for (DetectedObject detectedObject : detectedObjects) {
                              for (DetectedObject.Label label : detectedObject.getLabels()) {
                                  String text = label.getText().toLowerCase(Locale.ROOT);
                                  //int index = label.getIndex(); //not needed currently
                                  //float confidence = label.getConfidence(); //not needed currently
                                  Log.i("OBJECT STRING", text); //can delete later
                                  if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                      Log.i("FOUND!!!", "IN OD"); //can delete later
                                      submissionAccepted = 100;
                                  }
                              }}
                      }else{
                          Log.i("LOGGING: ", "NO OBJECTS FOUND");
                      }
                  }
                })
                    //When the process completes, create and call the Labeler processor
                    .addOnCompleteListener(new OnCompleteListener<List<DetectedObject>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<DetectedObject>> task) {
                            createLabelerImageProcessor();
                        }
                    });
        } else {
            Log.i("LOGGING: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
    }

    //Runs Labeler processor on finalBitmap, if the prompt is found, update submissionAccepted. Update the finalFile and swap to appropriate fragment (TCC or TNI)
    public void RunLabelerDetection(){
        InputImage image = InputImage.fromBitmap(finalBitmap, 0);
        if(imagelabeler != null){
            imagelabeler.process(image)
                    //If the process is successful, compare the objects to the prompt. If the prompt is found, update the submissionAccepted variable.
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            for (ImageLabel label : labels) {
                                String text = label.getText().toLowerCase(Locale.ROOT);
                                //float confidence = label.getConfidence();
                                //int index = label.getIndex();
                                Log.i("LOGGING: ", "FROM LABEL = " + text); // can be deleted later
                                if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                    Log.i("FOUND!!!", "IN LABELER"); //can be deleted later
                                    submissionAccepted = 100;
                                }
                            }
                        }
                    })
                    //After the image process is complete, if the submission was successful, update the finalFile with the bitmap and swap to the TCC fragment. Otherwise swap to TNI fragment.
                    .addOnCompleteListener(new OnCompleteListener<List<ImageLabel>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<ImageLabel>> task) {
                            if(submissionAccepted == 100){
                                finalFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
                                finalFile = bitmapToFile(getContext(), finalBitmap);
                                fragmentSwap(3);
                            }
                            else{
                                fragmentSwap(4);
                            }
                        }
                    });

        } else {
            Log.i("LOGGING: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
    }

    //Calls image processing functions to analyze image for appearance of prompt object
    public void analyzeImage(){
        //submissionAccepted variable is rest before analyzing the image
        submissionAccepted = 0;
        createCustomObjectDetectionImageProcessor();
    }

    //Launches the custom CameraActivity via intent
    public void launchCamera(){
        Intent intent = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(intent, 101);
    }

    //Switches the lower fragment on the MainScreenActivity based on code
    public void fragmentSwap(int code){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        homeHeaderFragment = new HomeHeaderFragment();
        switch(code){
            case 1:{
                homePromptFragment = new HomePromptFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView, homePromptFragment);
                break;
            }
            case 2:{
                homeImageDisplayFragment = new HomeImageDisplayFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView, homeImageDisplayFragment);
                break;
            }
            case 3:{
                homeAnalysisTCCFragment = new HomeAnalysisTCCFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView, homeAnalysisTCCFragment);
                break;
            }
            case 4:{
                homeAnalysisTNIFragment = new HomeAnalysisTNIFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView, homeAnalysisTNIFragment);
                break;
            }
            case 5:{
                homeTargetSpyedFragment = new HomeTargetSpyedFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView, homeTargetSpyedFragment);
                break;
            }
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }
}