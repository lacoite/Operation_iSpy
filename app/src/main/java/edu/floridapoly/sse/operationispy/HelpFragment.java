package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpFragment extends Fragment {
    //Declare views and button
    View view;
    ImageButton closeButton;
    TextView helpTextView;

    //Text for screen
    String helpText = "A new target identity will be released at a random time every day. Every agent receives the same prompt at the same time. You will receive a notification when the target's identity is released.\n\nIf you denied NOTIFICATION PERMISSIONS, please grant them in your DEVICE SETTINGS. Please also enable CAMERA PERMISSIONS.\n\nUse the \"TARGET IDENTIFIED\" button to launch the camera when you have found the target object. Take a picture of the target and use the \"ANALYZE CAPTURE\" button to submit, or the \"RECAPTURE\" button to retake the picture.\n\nIf the target is identified in your capture, you will earn ASSETS. Your assets are displayed at the top of your screen. The quicker you submit a valid capture, the more Assets you can earn. You can earn up to 1,000 Assets for a target.\n\nIf the target cannot be identified in your image, you can recapture the target. Avoid taking unclear, blurry, or otherwise distorted captures with multiple non-target objects.\n\nYou can view your rank using the \"AGENT RANK\" button. Work quickly to climb the ranks.\n\nUse the \"?\" icon to access the settings page to change your Agent Name or log out.\n\n Good luck agent!";

    public HelpFragment() {
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
        view = inflater.inflate(R.layout.fragment_help, container, false);
        helpTextView = view.findViewById(R.id.helpText);
        helpTextView.setText(helpText);

        closeButton = view.findViewById(R.id.closeHelpButton);
        //When the graphic close button is clicked, use the closeHelp function in main to make the fragment invisible and make the mainLayout visible
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainScreenActivity)getActivity()).closeHelp();
            }
        });

        return view;
    }
}