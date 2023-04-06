package edu.floridapoly.sse.operationispy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

//This fragment loads after a user submits a successful image and the assets have been updated (app can launch to this fragment)
public class HomeTargetSpyedFragment extends Fragment {
    //Declare views and button
    View view;
    TextView timeTextView;
    TextView promptTextView;
    Button copyToClipBoardButton;

    //Declare prompt and submittedTime variable
    long submittedTime;
    String submittedTimeString;
    String prompt;

    public HomeTargetSpyedFragment() {
        // Required empty public constructor
    }

    //Disable device back button for fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        view = inflater.inflate(R.layout.fragment_home_target_spyed, container, false);
        timeTextView = view.findViewById(R.id.timeTextView);
        promptTextView = view.findViewById(R.id.promptTextView);
        //Set submittedTime to the amount of time user took to submit a valid image, calculated by MainScreenActivity
        submittedTime = ((MainScreenActivity)getActivity()).getSubmissionTime();
        //Set prompt to prompt pulled in MainScreenActivity
        prompt = ((MainScreenActivity)getActivity()).getPrompt();

        //Set prompt text
        promptTextView.setText(prompt);
        //Set time textView to "IN {hour}H, {min}M, {sec}S" format of submitted time
        submittedTimeString = "IN " + String.valueOf((int)submittedTime/60/60) + "H " + String.valueOf(((int)submittedTime/60)%60) + "M " + String.valueOf((int)submittedTime%60) + "S";
        timeTextView.setText(submittedTimeString);

        //Sets onClick for button to save the "I Spyed a {prompt} in {hour}h {min}m {sec}" to device clipboard
        copyToClipBoardButton = view.findViewById(R.id.copyButton);
        copyToClipBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)((MainScreenActivity)getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Capture Data", "I Spyed a " + prompt + " " + timeTextView.getText().toString().toLowerCase(Locale.ROOT));
                clipboard.setPrimaryClip(clip);
            }
        });

        return view;
    }
}