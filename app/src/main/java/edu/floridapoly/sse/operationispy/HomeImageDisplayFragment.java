package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class HomeImageDisplayFragment extends Fragment {
    View view;
    ImageView imageView;
    Button analyzeCaptureButton;
    Button recaptureButton;

    public HomeImageDisplayFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeImageDisplayFragment newInstance(String param1, String param2) {
        HomeImageDisplayFragment fragment = new HomeImageDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        try{
//            view = inflater.inflate(R.layout.fragment_home_image_display, container, false);
//            imageView = view.findViewById(R.id.imageView);
//            imageView.setImageBitmap(MainScreenActivity.getBitmap());
//            //imageView.setImageBitmap(BitmapFactory.decodeFile(MainScreenActivity.getFile().getPath()));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
        //Inflate the layout and set image view to the rotatedBitmap
        view = inflater.inflate(R.layout.fragment_home_image_display, container, false);
        imageView = view.findViewById(R.id.imageView);
        imageView.setImageBitmap(MainScreenActivity.getBitmap());

        //Set onClick for analyzeCaptureButton and recaptureButton
        analyzeCaptureButton = view.findViewById(R.id.analyzeCaptureButton);
        recaptureButton = view.findViewById(R.id.recaptureButton);
        //When the button is clicked, start the camera activity, then wait for the response code
        analyzeCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainScreenActivity)getActivity()).analyzeImage();
            }
        });
        recaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainScreenActivity)getActivity()).launchCamera();
            }
        });

        return view;
    }
}