package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class SettingsFragment extends Fragment {
    //Declare views and buttons
    View view;
    ImageButton closeButton;
    Button logOutButton;

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

        //When the graphic close button is clicked, use the closeSettings function in main to make the fragment invisible and make the mainLayout visible
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainScreenActivity)getActivity()).closeSettings();
            }
        });

        //TODO: When the logout button is clicked, make user log out of account, return to sign in screen
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }
}