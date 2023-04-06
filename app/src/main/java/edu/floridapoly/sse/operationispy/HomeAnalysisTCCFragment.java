package edu.floridapoly.sse.operationispy;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//This fragment loads if an image submission is successful
public class HomeAnalysisTCCFragment extends Fragment {
    //Declare views
    View view;
    TextView assetsTextView;

    //Declare assets variable
    int assets;

    public HomeAnalysisTCCFragment() {
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
        view = inflater.inflate(R.layout.fragment_home_analysis_tcc, container, false);
        assetsTextView = view.findViewById(R.id.assetsTextView);

        //Set assets to the assets calculated in MainScreenActivity function
        assets = ((MainScreenActivity)getActivity()).getCalculatedAssets();

        //If the assets is 1000, set the assetsTextView to 1,000(done for formatting), otherwise set it to the assets
        if(assets == 1000){
            assetsTextView.setText("1,000");
        }
        else{
            assetsTextView.setText(String.valueOf(assets));
        }

        return view;
    }
}