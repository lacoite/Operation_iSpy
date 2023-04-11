package edu.floridapoly.sse.operationispy;

import static java.lang.Math.round;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
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
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;


public class MainScreenActivity extends AppCompatActivity {

    //Declare context variable
    static Context context;

    //Declare object detector and image labeler
    private ObjectDetector objectDetector;
    private ImageLabeler imagelabeler;

    //Declare FireStore
    static FirebaseFirestore db;

    //Declare files and bitmap for captured images
    //File path: "/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png"
    static File finalFile = null;
    static Bitmap tempBitmap; //improper orientation on generation
    static File tempFile = null;
    static Bitmap finalBitmap; //stores bitmap with corrected orientation

    //Declare variables for functions
    public static String prompt; //stores prompt word from firebase
    public static String promptComplete; //true or false based on "Submitted" field in database
    public static int submissionAccepted; //tracks whether object was found in image analysis
    public static long lastReleaseTime; //stores the prompt release time in seconds from firebase
    public static long submissionTime; //stores the time the user submitted an image for analysis
    public static int calculatedAssets; //assets earned from capture
    public static long subbed = -1000; //calculated time for how long it took user to submit image
    public static long currentAssets; //stores number of assets user has from firebase

    //Declare header buttons
    Button ranksButton;
    Button helpButton;
    ImageButton settingsButton;

    //Declare changing text views
    TextView loadingText;
    TextView totalAssetsText;
    TextView usernameText;

    //Declare fragments swapped in R.id.fragmentContainerView
    Fragment homePromptFragment;
    Fragment homeImageDisplayFragment;
    Fragment homeAnalysisTCCFragment;
    Fragment homeAnalysisTNIFragment;
    Fragment homeTargetSpyedFragment;

    //Declare overlay fragments for help, settings, and rank screen
    static FragmentContainerView helpFragment;
    static FragmentContainerView settingsFragment;
    static FragmentContainerView ranksFragment;

    //Declare layout for main activity and connection error
    static LinearLayout mainLayout;
    LinearLayout connectionLayout;

    static String userID;
    static String userName;

    //Intent and service for notifications to run in the background
    static Intent mNotificationServiceIntent;
    private NotificationService mNotificationService;

    //Begin onSnapshot listener in background when the app is closed to monitor for prompt releases
    @Override
    protected void onDestroy() {
        //stopService(mNotificationServiceIntent)
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    //On resume (from CameraActivity or app closing), run start function if the current fragment is not the HomeImageDisplayFragment, delay to allow fragment to update
    @Override
    protected void onResume(){
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm =  getSupportFragmentManager();
                Fragment fragInstance = fm.findFragmentById(R.id.fragmentContainerView);
                if(fragInstance instanceof HomeImageDisplayFragment){
                }
                else{
                    start();
                }
            }
        }, 2000);
    }

    //OnCreate, set screen mode, initialize views/buttons/layouts, ask for camera permission, start network monitor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        //Lock screen rotation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        context = getApplicationContext();

        SharedPreferences sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userID = sharedPref.getString("CurrentUserID", "NO_ID");

        Log.i("HERE: ", userID);
        if(userID.equals("NO_ID")){
            Intent signOutIntent = new Intent(getContext(), SignInActivity.class);
            signOutIntent.putExtra("SignOut", "true");
            startActivity(signOutIntent);
        }

        //Begins background notification service if it is not running
        mNotificationService = new NotificationService();
        mNotificationServiceIntent = new Intent(this, mNotificationService.getClass());
        if(!isNotificationSerivceRunnning(mNotificationService.getClass())){
            startService(mNotificationServiceIntent);
        }

        //Initialize buttons, views, layouts
        mainLayout = findViewById(R.id.mainLayout);
        connectionLayout = findViewById(R.id.connectionError);
        ranksButton = findViewById(R.id.ranksButton);
        helpButton = findViewById(R.id.helpButton);
        settingsButton = findViewById(R.id.settingsButton);
        usernameText = findViewById(R.id.usernameTextView);
        totalAssetsText = findViewById(R.id.totalAssetsTextView);
        loadingText = findViewById(R.id.loadingText);

        //Set loadingTextView to visible (prevents empty fragment container while things load)
        loadingText.setVisibility(View.VISIBLE);

        //Start a networkRequest and connectivityManager to monitor network connection
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        //Initialize Firestore connection
        db = FirebaseFirestore.getInstance();

        //Set onClicks for Settings, Help, and Ranks button in header. Each set their respective fragment to visible and hides mainLayout
        ranksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ranksFragment = findViewById(R.id.ranksFragmentContainerView);
                mainLayout.setVisibility(View.INVISIBLE);
                //Uses function in fragment to refresh the rank data before making the fragment visible
                RanksFragment.refreshRanks();
                ranksFragment.setVisibility(View.VISIBLE);
            }
        });
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpFragment = findViewById(R.id.helpFragmentContainerView);
                mainLayout.setVisibility(View.INVISIBLE);
                helpFragment.setVisibility(View.VISIBLE);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsFragment = findViewById(R.id.settingsFragmentContainerView);
                mainLayout.setVisibility(View.INVISIBLE);
                settingsFragment.setVisibility(View.VISIBLE);
            }
        });

        //If camera permissions are not allowed, request them from the user
        if ((ContextCompat.checkSelfPermission(MainScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(MainScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{
                    Manifest.permission.CAMERA}, 101);
        }

    }

    //Functions used in Ranks, Setting, and Help Fragments during onClick for graphic back button. Hides the mainLayout to prevent buttons being clickable in background
    public static void closeHelp(){
        helpFragment.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }
    public static void closeSettings(){
        settingsFragment.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }
    public static void closeRanks(){
        ranksFragment.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    //Detects if the notification service is running
    private boolean isNotificationSerivceRunnning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                Log.i("Service Status: ", "Running");
                return true;
            }
        }
        Log.i("Service Status: ", "Not Running");
        return false;
    }

    //Function called when the main activity resumes (on open as well due to activity lifecycle)
    //Pulls prompt, user data, and whether the user has made a successful submission
    public void start(){
        try{
            //Initialize Firestore (duplicated here to prevent error)
            db = FirebaseFirestore.getInstance();

            //Access Prompts Collection, specified Document path for Prompt, saves prompt name to variable
            DocumentReference docRefForPrompt = db.collection("ServerInfo").document("Releases");
            docRefForPrompt.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            prompt = document.getData().get("LastPrompt").toString();
                        } else {
                            Log.d("Prompt Pull Failed:", "No such document");
                        }
                    } else {
                        Log.d("DofRefForPrompt Failed: ", "get failed with ", task.getException());
                    }
                }
            });
            //Access User collection, specified ID path
            //Get whether or not the user has successfully submitted an image. Load PromptFragment if not, otherwise load TargetSpyedFragement
            //Save amount of assets user has to variable, save userUsername, update both in header
            DocumentReference docRefForCompletedAndAssets = db.collection("User").document(userID);
            docRefForCompletedAndAssets.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userName = document.getData().get("Username").toString();
                            promptComplete = document.getData().get("Submitted").toString();
                            currentAssets = (long)document.getData().get("Assets");
                            //If the user has not completed the prompt, hide the loading text, update the assets in the header, swap to HomePromptDisplayFragment
                            if(promptComplete.equals("false")){
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        usernameText.setText("Agent " + userName);
                                        //Format the assets value to have appropriate commas, set the totalAssetsText to the formatted string
                                        DecimalFormat df = new DecimalFormat("#,###");
                                        String formattedAssets = df.format(currentAssets);
                                        totalAssetsText.setText(formattedAssets);

                                        loadingText.setVisibility(View.INVISIBLE);
                                        fragmentSwap(1);
                                    }
                                }, 250);

                            }
                            //If the user has completed the prompt, recalculate time it took for accepted submission. hide the loading text, update the assets in the header, swap to the HomeTargetSpyedFragment
                            else if(promptComplete.equals("true")){
                                //Function updates the values used by the fragment to display the time it took to submit an accepted capture
                                recalculateTimes();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        usernameText.setText("Agent " + userName);
                                        //Format the assets value to have appropriate commas, set the totalAssetsText to the formatted string
                                        DecimalFormat df = new DecimalFormat("#,###");
                                        String formattedAssets = df.format(currentAssets);
                                        totalAssetsText.setText(formattedAssets);

                                        loadingText.setVisibility(View.INVISIBLE);
                                        fragmentSwap(5);
                                    }
                                }, 250);
                            }
                        } else {
                            Log.d("Submission/Assets Pull Failed: ", "No such document");
                        }
                    } else {
                        Log.d("DocRefFCaS Failed:", "get failed with ", task.getException());
                    }
                }
            });
        }catch (Exception e){

        }

    }

    //Hides the mainLayout if connection is interrupted, displays the connection error layout until connection is restored
    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            mainLayout.setVisibility(View.VISIBLE);
            connectionLayout.setVisibility(View.INVISIBLE);
            start();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainLayout.setVisibility(View.INVISIBLE);
                    connectionLayout.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

    //Returns application's context
    public static Context getContext(){
        return context;
    }

    //Returns corrected bitmap
    public static Bitmap getBitmap(){
        return finalBitmap;
    }

    //Returns prompt string
    public static String getPrompt(){
        return prompt;
    }

    //Returns calculatedAssets
    public static int getCalculatedAssets(){
        return calculatedAssets;
    }


    //Returns how long in seconds the user took to submit a valid capture
    public static int getSubmissionTime(){
        return (int)(submissionTime - lastReleaseTime)/1000;
    }

    //Pulls prompt release time and user submission time, function used to prevent variables if app is closed after submission is complete
    public void recalculateTimes(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Access ServerInfo collection, Releases document to pull the time that the prompt was released in seconds
                DocumentReference docRefForTime = db.collection("ServerInfo").document("Releases");
                docRefForTime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                lastReleaseTime = document.getDate("LastReleaseTime").getTime();
                            } else {
                                Log.d("Time Pull Failed:", "No such document");
                            }
                        } else {
                            Log.d("DocRefForTime Failed: ", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Access User collection, user ID document to pull the time that a successful capture was submitted
                DocumentReference docRefForSubmissionTime = db.collection("User").document(userID);
                docRefForSubmissionTime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                submissionTime = document.getDate("TimeSubmitted").getTime();
                            } else {
                                Log.d("SubmissionTime Pull Failed:", "No such document");
                            }
                        } else {
                            Log.d("DocRefForSubmissionTime Failed:", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });
    }

    //When the Camera Activity ends, update the tempBitmap and finalBitmap and switch to the image display fragment if an image was captured. Otherwise switch to the prompt fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //101 = image was captured
        if(resultCode == 101) {
            tempBitmap =  BitmapFactory.decodeFile("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
            rotateBitmap(tempBitmap);
            fragmentSwap(2);
        }
        //202 = image not captured, user used device back button on camera activity
        else if(resultCode == 202) {
            fragmentSwap(1);
        }
    }

    //Sets the finalBitmap to the orientation-corrected tempBitmap
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

    //Calls image processing functions to analyze image for appearance of prompt object
    public void analyzeImage(){
        //submissionAccepted variable is rest before analyzing the image
        submissionAccepted = 0;
        createCustomObjectDetectionImageProcessor();
    }

    //Creates and runs Custom Object Detector image processor from ML Kit
    public void createCustomObjectDetectionImageProcessor(){
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
        ImageLabelerOptions imageLabelerOptions = new ImageLabelerOptions.Builder().build();
        imagelabeler = ImageLabeling.getClient(imageLabelerOptions);
        RunLabelerDetection();
    }

    //Runs Object Detection processor on the finalBitmap, update submissionAccepted, and loading text if prompt is found then call method to create and run Labeler processor.
    public void RunCustomObjectDetection(){
        InputImage image = InputImage.fromBitmap(finalBitmap, 0);
        if(objectDetector != null){
            objectDetector.process(image)
                    //If process is successful, compare the objects to the prompt. If the prompt is found, update the submissionAccepted variable
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                  @Override
                  public void onSuccess(List<DetectedObject> detectedObjects) {
                      if(detectedObjects.isEmpty() != true){
                          // Temporarily change the loadingText to "Analyzing", make it visible,
                          loadingText.setText("Analyzing...");
                          loadingText.setVisibility(View.VISIBLE);
                          for (DetectedObject detectedObject : detectedObjects) {
                              for (DetectedObject.Label label : detectedObject.getLabels()) {
                                  //Check each returned label in lowercase to the prompt in lowercase. If the prompt is found update the submissionAccepted variable
                                  String text = label.getText().toLowerCase(Locale.ROOT);
                                  loadingText.setText("Analyzing...");
                                  loadingText.setVisibility(View.VISIBLE);
                                  if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                      submissionAccepted = 100;
                                  }
                              }}
                      }else{
                         //No prompt object found in labels, continue
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
            Log.i("Object Detection Failed: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
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
                            // Temporarily change the loadingText to "Analyzing", make it visible,
                            loadingText.setText("Analyzing...");
                            loadingText.setVisibility(View.VISIBLE);
                            for (ImageLabel label : labels) {
                                //Check each returned label in lowercase to the prompt in lowercase. If the prompt is found update the submissionAccepted variable
                                String text = label.getText().toLowerCase(Locale.ROOT);
                                if(text.contains(prompt.toLowerCase(Locale.ROOT))){
                                    submissionAccepted = 100;
                                }
                            }
                        }
                    })
                    //After the image process is complete, if the submission was successful, update the finalFile with the bitmap and swap to the TCC fragment. Otherwise swap to TNI fragment.
                    .addOnCompleteListener(new OnCompleteListener<List<ImageLabel>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<ImageLabel>> task) {
                            //If the submission is accepted, display the analyzing text while: update the submitted value and time in firebase, calculate earned assets; then swap to the TCCFragment, wait several seconds, then swap to the TargetSpyedFragment
                            if(submissionAccepted == 100){
                                //Update submitted value and time in firebase
                                updateDbSubmitted();
                                //Calculate earned assets
                                calculateScore();

                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentSwap(3);
                                    }
                                }, 2000);
                                Handler handler2 = new Handler(Looper.getMainLooper());
                                handler2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingText.setVisibility(View.INVISIBLE);
                                        loadingText.setText("Loading...");
                                    }
                                }, 2500);
                                Handler handler3 = new Handler(Looper.getMainLooper());
                                handler3.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentSwap(5);
                                    }
                                }, 6000);
                            }
                            //If the submission is not accepted, swap to the TNIFragment, wait 3 seconds, then swap to the HomePromptfragment
                            else{
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentSwap(4);
                                    }
                                }, 1500);
                                Handler handler2 = new Handler(Looper.getMainLooper());
                                handler2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingText.setVisibility(View.INVISIBLE);
                                        loadingText.setText("Loading...");
                                    }
                                }, 2000);
                                Handler handler3 = new Handler();
                                handler3.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentSwap(1);
                                    }
                                }, 5000);

                            }
                        }
                    });

        } else {
            Log.i("Labeler Failed: ", "Null imageProcessor, please check adb logs for imageProcessor creation error");
        }
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
        fragmentTransaction.commitAllowingStateLoss();
    }

    //Updates the Assets Field for user in User collection
    public static void updateAssetsInDb(){
        DocumentReference docRef = db.collection("User").document(userID);
        // Update the Assets field to increment by the number of assets earned for submission
        docRef.update("Assets", FieldValue.increment(calculatedAssets));
    }

    //Updates the TimeSubmitted field for the user in the User collection
    public void saveTimeStamp() {
        DocumentReference docRef = db.collection("User").document(userID);
        // Update the timestamp field with the value from the server
        docRef.update("TimeSubmitted", FieldValue.serverTimestamp());
    }

    //Updates the Submitted field for the user in the user table
    public void updateDbSubmitted() {
        DocumentReference docRef = db.collection("User").document(userID);
        // Update the Submitted field to be true
        docRef.update("Submitted", "true");
    }

    //Updates the Username field for the user in the user table
    public void updateDbUsername(String newUser) {
        DocumentReference docRef = db.collection("User").document(userID);
        // Update the Username field to be the new username
        docRef.update("Username", newUser);
        //Update the UI
        usernameText.setText("Agent " + newUser);
    }

    //Update user's notified box to prevent user from receiving duplicate notifications
    public void updateDbNotified() {
        DocumentReference docRef = db.collection("User").document(userID);
        // Update the Notified field to be 0
        docRef.update("Notified", 0);
    }

    //Calculates how many assets user earns based on time between prompt release and successful submission
    public void calculateScore(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Access ServerInfo collection, Releases document to pull the time that the prompt was released in seconds
                DocumentReference docRefForTime = db.collection("ServerInfo").document("Releases");
                docRefForTime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                lastReleaseTime = document.getDate("LastReleaseTime").getTime();
                            } else {
                                Log.d("Time Pull Failed:", "No such document");
                            }
                        } else {
                            Log.d("DocRefForTime Failed: ", "get failed with ", task.getException());
                        }
                    }
                });
                //Access User collection, user ID document to pull the time that user submitted valid capture
                DocumentReference docRefForSubmissionTime = db.collection("User").document(userID);
                docRefForSubmissionTime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                submissionTime = document.getDate("TimeSubmitted").getTime();
                            } else {
                                Log.d("Submission Time Pull Failed", "No such document");
                            }
                        } else {
                            Log.d("DocRefForSubmissionTime Failed:", "get failed with ", task.getException());
                        }
                    }
                });
            }
        }, 1000);

        //Updates totalAssetsText in header with calculated earned assets
        Handler handler2 = new Handler(Looper.getMainLooper());
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Amount of time in seconds between prompt release and user submission (/1000 to remove ms)
                subbed = (submissionTime - lastReleaseTime)/1000;
                //User losed 0.015 points for every second, with 1 point buffer and result rounded
                calculatedAssets = (int) round(1000 - (subbed)*0.015)+1;
                //Minimum points earned is 0, max is 1000
                if(calculatedAssets < 0){
                    calculatedAssets = 0;
                }else if(calculatedAssets > 1000){
                    calculatedAssets = 1000;
                }
                //Update user assets field in firebase
                updateAssetsInDb();
                //Update the current assets to increment by calculated assets
                currentAssets += calculatedAssets;
                //Format calculated assets to include comme, set totalAssetsText to the formatted string
                DecimalFormat df = new DecimalFormat("#,###");
                String formattedAssets = df.format(currentAssets);
                totalAssetsText.setText(formattedAssets);
            }
        },2000);
    }
}