package edu.floridapoly.sse.operationispy;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth.AbstractOAuthGetToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    //Declare firestore variable
    static FirebaseFirestore db;

    //Declare views, buttons, and edit text
    View view;
    ImageButton closeButton;
    Button logOutButton;
    Button confirmNameChangeButton;
    EditText enteredNameEditText;

    //Int for whether username is already taken
    static int userTaken;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        closeButton = view.findViewById(R.id.closeSettingsButton);
        logOutButton = view.findViewById(R.id.logOutButton);
        confirmNameChangeButton = view.findViewById(R.id.confirm_name_button);
        enteredNameEditText = view.findViewById(R.id.username_edittext);

        //When the graphic close button is clicked, use the closeSettings function in main to make the fragment invisible and make the mainLayout visible
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainScreenActivity)getActivity()).closeSettings();
            }
        });


        //When the confirmNameChange button is clicked, ensure that the user entered a unique, non-null name, that is less than 9 characters, otherwise Toast
        confirmNameChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close keyboard if open
                View keyview = ((MainScreenActivity)getActivity()).getCurrentFocus();
                if (keyview != null) {
                    InputMethodManager manager
                            = (InputMethodManager)
                            ((MainScreenActivity)getActivity()).getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    manager
                            .hideSoftInputFromWindow(
                                    view.getWindowToken(), 0);
                }

                String newUser = enteredNameEditText.getText().toString();
                Log.i("USERNAME IS ", newUser);
                if(!newUser.equals("") && newUser.length()-1 <= 10){
                    userTaken = 0;
                    try{
                        //Initialize Firestore
                        db = FirebaseFirestore.getInstance();

                        //Query the firebase db for usernames to ensure username is not already taken
                        Query docRefForUser = db.collection("User");
                        docRefForUser.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            //For each document returned
                                            for(QueryDocumentSnapshot document : task.getResult()){
                                                //If the entered name is found, update the userTaken variable
                                                if((document.getString("Username").toLowerCase(Locale.ROOT).equals(newUser.toLowerCase(Locale.ROOT)))||(newUser.toLowerCase(Locale.ROOT).equals("no_id"))){
                                                    userTaken = 1;
                                                }
                                            }
                                            //If the username is taken, alert the user and clear the field
                                            if(userTaken == 1){
                                                Toast.makeText(getContext(), "Agent Name Is Already Taken", Toast.LENGTH_LONG).show();
                                                enteredNameEditText.setText("");
                                            }
                                            //If the user is valid, update the username in the database and close the settings
                                            else{
                                                enteredNameEditText.setText("");
                                                ((MainScreenActivity)getActivity()).updateDbUsername(newUser);
                                                closeButton.callOnClick();
                                            }
                                        }
                                        else{
                                            Log.i("Error Getting Documents", "");
                                        }
                                    }
                                });
                    }
                    catch(Exception e){
                    }
                }
                //If the entered name is null or over 10 characters, alert the user and clear the field
                else{
                    Toast.makeText(getContext(), "Agent Name Must Be At Least 1 Character But Less Than 10", Toast.LENGTH_LONG).show();
                    enteredNameEditText.setText("");
                }
            }
        });

        //When the logout button is clicked, make user log out of account, return to sign in screen
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signOutIntent = new Intent(getContext(), SignInActivity.class);
                signOutIntent.putExtra("SignOut", "true");
                startActivity(signOutIntent);
            }
        });
        return view;
    }
}