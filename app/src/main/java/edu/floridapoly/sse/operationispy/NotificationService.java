package edu.floridapoly.sse.operationispy;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class NotificationService extends Service {
    public int counter = 0;
    static FirebaseFirestore db;
    Handler handler;
    String userID;
    static NotificationManager notificationManager;
    static NotificationManager notificationManager2;
    static String NOTIFICATION_CHANNEL_ID;

    @Override
    public void onCreate() {
        super.onCreate();

        //Build notification channels
        NOTIFICATION_CHANNEL_ID = "Example.Permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.enableVibration(false);
        //chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(chan);

        notificationManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("default", "Operation iSpy", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification from Operation iSpy");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager2.createNotificationChannel(channel);
        }


        startNotificationForeground();
    }

    //Foreground notification letting user know that Operation iSPy will continue in the background to monitor the prompts
    private void startNotificationForeground() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.app_logo);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Operation iSpy is Running in the Background to Notify You When A Prompt is Released ")
                .setPriority(NotificationManager.IMPORTANCE_NONE)
                .setSilent(true)
                .setLargeIcon(icon)
//                .setSmallIcon(R.mipmap.app_logo_round)
                .setVibrate(null)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    //Initialize onSnapshot function to monitor the database for a prompt change. Release notification when field changes to specified value
    //TODO: update onSnapshot to read from the User's notified box OR ServerInfo notified box
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        SharedPreferences sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userID = sharedPref.getString("CurrentUserID", "NO_ID");
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                db = FirebaseFirestore.getInstance();
                final DocumentReference docRef = db.collection("User").document(userID);
                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            Log.d(TAG, "Current data: " + snapshot.getData());
                            if(String.valueOf(snapshot.get("Notified")).equals("1")){
                                Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default")
                                        .setSmallIcon(R.mipmap.app_logo_round)
                                        .setAutoCancel(true)
                                        .setContentTitle("Operation iSpy")
                                        .setContentText("New Target Profile Released")
                                        .setContentIntent(pendingIntent)
                                        .setPriority(NotificationCompat.PRIORITY_MAX);

                                //Check that the userID has not been set to NO_ID, indicating that the user has signed out.
                                //If the user is signed in and the prompt has been released, send the notification and update the Notified field in the user document
                                SharedPreferences sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                                String userCheck = sharedPref.getString("CurrentUserID", "NO_ID");
                                if(!userCheck.equals("NO_ID")){
                                    notificationManager2.notify(3, builder.build());
                                    DocumentReference docRef = db.collection("User").document(userID);
                                    // Update the Notified field to be 0
                                    docRef.update("Notified", 0);
                                }
                                //If the user has logged out, stop the notification service
                                else{
                                    stopSelf();
                                }
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    }
                });
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

}