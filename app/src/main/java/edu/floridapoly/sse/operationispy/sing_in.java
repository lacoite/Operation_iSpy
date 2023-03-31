package edu.floridapoly.sse.operationispy;


import static android.content.ContentValues.TAG;

import static edu.floridapoly.sse.operationispy.MainScreenActivity.getContext;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;



public class sing_in extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    SignInButton signInButton;
    Button signOutButton;
    TextView statusTextView;
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN=9001;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        statusTextView = (TextView) findViewById(R.id.status_textview);
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        signInButton.setEnabled(true);


    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sign_in_button:
                signIn();

                break;
            case R.id.signOutButton:
                signOut();
                signInButton.setEnabled(true);
                break;
        }

    }



    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResults(result);
        }
    }

    private void handleSignInResults(GoogleSignInResult result){
        Log.d(TAG,"handleSignInResult: " + result.isSuccess());
        if(result.isSuccess()){
            GoogleSignInAccount acct = result.getSignInAccount();


            statusTextView.setText("Hello, " + acct.getDisplayName());
            signInButton.setEnabled(false);
            Log.d(TAG, "Email : " + acct.getEmail());
            FirebaseFirestore db = FirebaseFirestore.getInstance();



                Map<String, Object> data = new HashMap<>();
            Date currentTime = Calendar.getInstance().getTime();

                data.put("Username: ",  acct.getDisplayName() );
                data.put("email",  acct.getEmail() );
            data.put("TimeSubmitted: ",  currentTime );
            data.put("Submitted: ",  "false" );



            DocumentReference newUserRef = db.collection("User").document();

                db.collection("User").document(newUserRef.getId()).set(data);

                Log.d(TAG, "The ID: "+ newUserRef.getId());

            Intent MainScreenActivity = new Intent(getApplicationContext(), MainScreenActivity.class);
            startActivity(MainScreenActivity);


        }else{
            Log.d(TAG, "Please try logging in again");

        }
    }




    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.d(TAG, "onConnectionFailed; " + connectionResult);
    }
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                statusTextView.setText("Signed Out");
            }

        });

    }


}






