package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//This fragment loads when the app is open if the user has not successfully submitted an image
public class HomePromptFragment extends Fragment {
    //Declare views and buttons
    View view;
    Button targetIdentifiedButton;
    TextView promptText;

    public HomePromptFragment() {
        // Required empty public constructor
    }

    //Disable device back button for fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_prompt, container, false);

        //Set promptText to the current prompt
        promptText = view.findViewById(R.id.promptText);
        promptText.setText(((MainScreenActivity)getActivity()).getPrompt());

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