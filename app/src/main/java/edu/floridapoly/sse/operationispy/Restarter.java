package edu.floridapoly.sse.operationispy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {
    Intent serviceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        serviceIntent = new Intent(context, NotificationService.class);
        SharedPreferences sharedPref = MainScreenActivity.getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String userID = sharedPref.getString("CurrentUserID", "");
        Log.i("RESTARTER ID: ", userID);
        Log.i("HERE: ", userID);
        if(userID.equals("NO_ID")){
            NotificationService.notificationManager.cancelAll();
            context.stopService(serviceIntent);
        }
        else{
            context.startForegroundService(serviceIntent);
        }
        NotificationService.notificationManager.cancelAll();
    }
}