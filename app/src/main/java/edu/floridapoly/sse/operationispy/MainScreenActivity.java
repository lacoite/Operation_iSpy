package edu.floridapoly.sse.operationispy;

//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class MainScreenActivity extends AppCompatActivity {


    static Context context;
    static File finalFile = null;
    static Bitmap bitmap;
    static File tempFile = null;
    static Bitmap tempBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();

        if(ContextCompat.checkSelfPermission(MainScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{
                    Manifest.permission.CAMERA}, 101);
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        Fragment homePromptFragment = new HomePromptFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView, homePromptFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


//        HomeImageDisplayFragment imageDisplayFragment = new HomeImageDisplayFragment();
//        fragmentTransaction.replace(R.id.fragmentContainerView, imageDisplayFragment);
//        //provide the fragment ID of your first fragment which you have given in
//        //fragment_layout_example.xml file in place of first argument
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();



    }

    public static File bitmapToFile(Context context,Bitmap bitmap) { // File name like "image.png"
        //create a file to write bitmap data
        try {
            tempFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/tempImage.png");
            tempFile.createNewFile();

//Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return tempFile;
        }catch (Exception e){
            e.printStackTrace();
            return tempFile; // it will return null
        }
    }


    public static Context getContext(){
        return context;
    }

    public static File getFile(){
        //return finalFile;
        return finalFile;
    }
    public static Bitmap getBitmap(){
        //return finalFile;
        return tempBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
            bitmap =  BitmapFactory.decodeFile("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
            rotateBitmap(bitmap);

            //finalFile = bitmapToFile(getContext(), bitmap);
            //finalFile = new File("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            Fragment homeImageDisplayFragment = new HomeImageDisplayFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, homeImageDisplayFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void rotateBitmap(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try{
            exifInterface = new ExifInterface("/data/data/edu.floridapoly.sse.operationispy/cache/currentImage.png");
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void removeFragment(){
        Intent intent = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(intent, 101);

//        Fragment oldFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
//        if (oldFragment != null) {
//            getSupportFragmentManager().beginTransaction()
//                    .remove(oldFragment).commit();
//        }


        ////THIS CHUNK WORKS, IT REPLACES THE FRAGMENT
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//        HomeImageDisplayFragment homeImageFragment = new HomeImageDisplayFragment();
//        fragmentTransaction.replace(R.id.fragmentContainerView, homeImageFragment);
//        //provide the fragment ID of your first fragment which you have given in
//        //fragment_layout_example.xml file in place of first argument
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//



//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView));
//        //fragmentTransaction.remove(homePromptFragment).commit();
//        if(current != null){
//            Toast.makeText(getContext(), "Found Fragment", Toast.LENGTH_SHORT).show();
//            getSupportFragmentManager().beginTransaction().remove(current).commit();
//        }
//        else{
//            Toast.makeText(getContext(), "No Fragment", Toast.LENGTH_SHORT).show();
//
//        }

        //provide the fragment ID of your first fragment which you have given in
        //fragment_layout_example.xml file in place of first argument
        //fragmentTransaction.addToBackStack(null);
        //fragmentTransaction.commit();

    }
}