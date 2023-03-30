package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomePromptFragment extends Fragment {
    View view;
    Button targetIdentifiedButton;

    public HomePromptFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_prompt, container, false);

        Log.i("CURRENTLY IN:", "HomePrompt Before button");
        //Set onClick for targetIdentified button
        targetIdentifiedButton = view.findViewById(R.id.targetIdentifiedButton);
        //When the button is clicked, start the camera activity, then wait for the response code
        targetIdentifiedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainScreenActivity)getActivity()).launchCamera();
            }
        });
        return view;
    }
}