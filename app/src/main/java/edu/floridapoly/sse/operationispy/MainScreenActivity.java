package edu.floridapoly.sse.operationispy;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
// TESTING FRAGMENT MANAGER
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//
//        HomeImageDisplayFragment imageDisplayFragment = new HomeImageDisplayFragment();
//        fragmentTransaction.replace(R.id.fragmentContainerView, imageDisplayFragment);
//        //provide the fragment ID of your first fragment which you have given in
//        //fragment_layout_example.xml file in place of first argument
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }
}