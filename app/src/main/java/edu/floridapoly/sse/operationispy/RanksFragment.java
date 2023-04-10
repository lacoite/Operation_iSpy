package edu.floridapoly.sse.operationispy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RanksFragment extends Fragment {
    //Declare firestore variable
    static FirebaseFirestore db;

    //Declare views
    static View view;
    static TextView rankName1, rankName2, rankName3, rankName4, rankName5; //values for table column
    static TextView asset1, asset2, asset3, asset4, asset5; //values for table columns
    static TextView yourRank; //user's rank
    static ImageButton closeButton;

    //Declare variables for refreshing the rank table
    static int place = 0;
    static int i;
    static List<String> userName;
    static List<String> assets;

    static String userID;

    public RanksFragment() {
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
        view = inflater.inflate(R.layout.fragment_ranks, container, false);
        closeButton = view.findViewById(R.id.closeHelpButton);

        //Pull stored UserID from shared preference
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userID = sharedPref.getString("CurrentUserID", "X");

        //When the graphic close button is clicked, use the closeRanks function in main to make the fragment invisible and make the mainLayout visible
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainScreenActivity)getActivity()).closeRanks();
            }
        });

        return view;
    }

    //Function used in MainScreenActivity when RanksFragment is made visible to list top 5 users by assets and find the user's position
    //TODO: replace user's name with variable
    public static void refreshRanks(){
        try{
            //Initialize Firestore
            db = FirebaseFirestore.getInstance();

            //Initialize userName and assets array lists
            userName = new ArrayList<>();
            assets = new ArrayList<>();

            //Initialize text views for user's rank, top 5 rankNames and assets
            yourRank = view.findViewById(R.id.yourRank);
            rankName1 = view.findViewById(R.id.rankName1);
            rankName2 = view.findViewById(R.id.rankName2);
            rankName3 = view.findViewById(R.id.rankName3);
            rankName4 = view.findViewById(R.id.rankName4);
            rankName5 = view.findViewById(R.id.rankName5);
            asset1 = view.findViewById(R.id.asset1);
            asset2 = view.findViewById(R.id.asset2);
            asset3 = view.findViewById(R.id.asset3);
            asset4 = view.findViewById(R.id.asset4);
            asset5 = view.findViewById(R.id.asset5);

            //Query the firebase db for Users in descending order by their Assets, saves top 5 to textviews, finds user's place
            Query docRefForUser = db.collection("User").orderBy("Assets", Query.Direction.DESCENDING);
            docRefForUser.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                //Reset variable i
                                i = 0;
                                //For each document returned
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    //For the first 5, add the username and formatted assets to the arrayLists
                                    if (i < 5){
                                        userName.add(String.valueOf(document.getString("Username")));
                                        DecimalFormat df = new DecimalFormat("#,###");
                                        String formattedAssets = df.format(document.get("Assets"));
                                        assets.add(formattedAssets);
                                    }
                                    //When the user's name is found, mark the place
                                    if(document.getId().equals(userID)){
                                        place = i+1;
                                    }
                                    i++;
                                }
                                //Set yourRank text to determined place
                                yourRank.setText(String.valueOf(place));
                                //Set usernames and assets for top 5 stored in arrayLists
                                try{
                                    rankName1.setText(userName.get(0));
                                    asset1.setText(assets.get(0));
                                    rankName2.setText(userName.get(1));
                                    asset2.setText(assets.get(1));
                                    rankName3.setText(userName.get(2));
                                    asset3.setText(assets.get(2));
                                    rankName4.setText(userName.get(3));
                                    asset4.setText(assets.get(3));
                                    rankName5.setText(userName.get(4));
                                    asset5.setText(assets.get(4));
                                }
                                catch (Exception e){

                                }
                            }
                            else{
                                Log.i("Error Getting Documents", "");
                            }
                        }
                    });
            }catch(Exception e){
        }
    }
}