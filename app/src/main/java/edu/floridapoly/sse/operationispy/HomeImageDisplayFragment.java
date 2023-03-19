package edu.floridapoly.sse.operationispy;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HomeImageDisplayFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;
    ImageView imageView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeImageDisplayFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeImageDisplayFragment newInstance(String param1, String param2) {
        HomeImageDisplayFragment fragment = new HomeImageDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    //TODO: CURRENTLY 1 IMAGE BEHIND, LIKELY A THREADING ISSUE, MUST BE FIXED
    //Sets imageView to currentImage.jpg
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        try{
            view = inflater.inflate(R.layout.fragment_home_image_display, container, false);
            imageView = view.findViewById(R.id.imageView);
            imageView.setImageBitmap(BitmapFactory.decodeFile("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.jpg"));
        } catch (Exception e){
            Log.i("IMAGE ERROR", "ERROR");
            e.printStackTrace();
        }

        return view;
    }
}