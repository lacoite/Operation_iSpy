package edu.floridapoly.sse.operationispy;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class retestFire extends AppCompatActivity {
    FirebaseFirestore db;
    //private FirebaseAuth mAuth;

    // Initialize Firebase Auth
    //mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retest_fire);

        db = FirebaseFirestore.getInstance();
        readFire();
        onSnapshot();
        setData();
    }

    void readFire() {
        db.collection("Leaderboard").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("MYTAG", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.w("MYTAG", "Error getting documents.", task.getException());
                }
            }
        });
    }
    void onSnapshot(){
        final DocumentReference docRef = db.collection("DailyPrompt").document("DateAndPrompt");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    void setData() {
        Map<String, Object> data = new HashMap<>();
        data.put("Username", "Tokyo");
        data.put("email", "Japan@gmail.com");

        DocumentReference newUserRef = db.collection("User").document();

        db.collection("User").document(newUserRef.getId()).set(data);

        Log.d(TAG, "The ID: "+ newUserRef.getId());
    }
}