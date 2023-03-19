package edu.floridapoly.sse.operationispy;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class HomePromptFragment extends Fragment {
    View view;
    Button targetIdentifiedButton;
    final int REQUEST_CODE = 1000;
    File file = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.jpg");


    public HomePromptFragment() {
        // Required empty public constructor
    }

    //Evaluate response code (Currently only one request code, will need more later
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            //Swaps the fragment to the image display
            case REQUEST_CODE:
               // if(file.exists()){
                    Toast.makeText(getActivity(),"Exists", Toast.LENGTH_LONG).show();

                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();

                    HomeImageDisplayFragment imageDisplayFragment = new HomeImageDisplayFragment();
                    fragmentTransaction.replace(R.id.fragmentContainerView, imageDisplayFragment);
                    //provide the fragment ID of your first fragment which you have given in
                    //fragment_layout_example.xml file in place of first argument
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
//                }
//                else{
//                    Toast.makeText(getActivity(),"Error creating file", Toast.LENGTH_LONG).show();
//
//                }
                break;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_prompt, container, false);

        //Set onClick for targetIdentified button
        targetIdentifiedButton = view.findViewById(R.id.targetIdentifiedButton);
        //When the button is clicked, start the camera activity, then wait for the response code
        targetIdentifiedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (file.exists()) {
//                    file.delete();
//                    Toast.makeText(getActivity(), "Cleared the Image", Toast.LENGTH_LONG).show();
//                }
                Intent intent = new Intent(getActivity(), CameraActivity.class);

                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        return view;


    }
}