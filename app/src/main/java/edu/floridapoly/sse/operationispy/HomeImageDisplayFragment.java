package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

//This fragment loads when an image is captured
public class HomeImageDisplayFragment extends Fragment {
    //Declare views and buttons
    View view;
    ImageView imageView;
    Button analyzeCaptureButton;
    Button recaptureButton;

    public HomeImageDisplayFragment() {
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
        //Inflate the layout and set image view to the rotatedBitmap
        view = inflater.inflate(R.layout.fragment_home_image_display, container, false);
        imageView = view.findViewById(R.id.imageView);
        //set imageView's bitmap to the captured image
        imageView.setImageBitmap(MainScreenActivity.getBitmap());

        analyzeCaptureButton = view.findViewById(R.id.analyzeCaptureButton);
        recaptureButton = view.findViewById(R.id.recaptureButton);

        //Set onClick for analyzeCaptureButton and recaptureButton
        //When analyzeCapture button is clicked, disable both buttons being clickable, save the time the button was clicked and analyze the image
        analyzeCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analyzeCaptureButton.setClickable(false);
                recaptureButton.setClickable(false);
                ((MainScreenActivity)getActivity()).saveTimeStamp();
                ((MainScreenActivity)getActivity()).analyzeImage();
            }
        });
        //When recapture button is clicked, disable both buttons being clickable, start the camera activity, then wait for the response code
        recaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analyzeCaptureButton.setClickable(false);
                recaptureButton.setClickable(false);
                ((MainScreenActivity)getActivity()).launchCamera();
            }
        });
        return view;
    }
}