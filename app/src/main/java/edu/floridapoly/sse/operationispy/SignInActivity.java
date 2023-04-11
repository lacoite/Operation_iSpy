package edu.floridapoly.sse.operationispy;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends AppCompatActivity implements
        View.OnClickListener {
    public static String savedId;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DocumentReference newUserRef = db.collection("User").document();
    static int accountFound = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Views
        mStatusTextView = findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        // [START customize_button]
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        // [END customize_button]
    }

    @Override
    public void onStart() {
        super.onStart();
        //If the user returned from the LogOut button, erase the user's name from the shared preference and sign the user out
        try{
            Intent signOutIntent = getIntent();
            if(signOutIntent.getStringExtra("SignOut").equals("true")){
                SharedPreferences sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("CurrentUserID", "NO_ID");
                            editor.commit();
                signOut();
                updateUI(null);
            };
        //If the user did not return from the LogOut button...
        }catch (Exception e){
            // [START on_start_sign_in]
            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            //If no account found, show signin UI
            if(account == null){
                updateUI(account);
            }
            //If account found, start MainScreenActivity
            else{
                Intent startMainScreenActivityIntent = new Intent(getApplicationContext(), MainScreenActivity.class);
                startActivity(startMainScreenActivityIntent);
            }
            // [END on_start_sign_in]
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]
    public static String encode(String s) {
        // create a string to add in the initial
        // binary code for extra security
        String ini = "11111111";
        int cu = 0;

        // create an array
        int arr[] = new int[11111111];

        // iterate through the string
        for (int i = 0; i < s.length(); i++) {
            // put the ascii value of
            // each character in the array
            arr[i] = (int) s.charAt(i);
            cu++;
        }
        String res = "";

        // create another array
        int bin[] = new int[111];
        int idx = 0;

        // run a loop of the size of string
        for (int i1 = 0; i1 < cu; i1++) {

            // get the ascii value at position
            // i1 from the first array
            int temp = arr[i1];

            // run the second nested loop of same size
            // and set 0 value in the second array
            for (int j = 0; j < cu; j++) bin[j] = 0;
            idx = 0;

            // run a while for temp > 0
            while (temp > 0) {
                // store the temp module
                // of 2 in the 2nd array
                bin[idx++] = temp % 2;
                temp = temp / 2;
            }
            String dig = "";
            String temps;

            // run a loop of size 7
            for (int j = 0; j < 7; j++) {

                // convert the integer to string
                temps = Integer.toString(bin[j]);

                // add the string using
                // concatenation function
                dig = dig.concat(temps);
            }
            String revs = "";

            // reverse the string
            for (int j = dig.length() - 1; j >= 0; j--) {
                char ca = dig.charAt(j);
                revs = revs.concat(String.valueOf(ca));
            }
            res = res.concat(revs);
        }
        // add the extra string to the binary code
        res = ini.concat(res);

        // return the encrypted code
        return res;
    }
    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            accountFound = 0;
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // set String s to get the account email when they sign in
            String s = account.getEmail();
            // Signed in successfully, show authenticated UI.
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query docRefForUser = db.collection("User");
            docRefForUser.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                // set a variable named hashedEmail to be the users email from google
                                String hashedEmail = encode(s);
                                //For each document returned
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    //If a user with the provided email is found, set the accountFound code and save the user's ID
                                    if(String.valueOf(document.getString("Email")).equals(hashedEmail)){
                                        accountFound = 100;
                                        savedId = document.getId();
                                    }
                                }
                                //After checking each user, if no account is found, create a new account with default data
                                if(accountFound == 0){
                                        String randomNum = String.valueOf(new Random().nextInt(9000) + 1000);
                                        Map<String, Object> data = new HashMap<>();
                                        Date currentTime = Calendar.getInstance().getTime();

                                        data.put("Assets", 0);
                                        data.put("Username" , "Agent" + randomNum);
                                        data.put("Email", hashedEmail);
                                        data.put("TimeSubmitted", currentTime);
                                        data.put("Submitted", "false");
                                        data.put("Notified",0);

                                        db.collection("User").document(newUserRef.getId()).set(data);

                                        Log.d(TAG, "The ID: " + newUserRef.getId());
                                        savedId = newUserRef.getId();
                                }
                            }
                            //Update the user's ID in the shared preference and start the MainScreenActivity
                            SharedPreferences sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("CurrentUserID", savedId);
                            editor.commit();
                            Intent startMainScreenActivityIntent = new Intent(getApplicationContext(), MainScreenActivity.class);
                            startActivity(startMainScreenActivityIntent);
                        }
                    });

            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }
}

