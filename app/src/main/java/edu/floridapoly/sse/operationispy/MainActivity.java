//package edu.floridapoly.sse.operationispy;
//
//import android.Manifest;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//import android.provider.MediaStore;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import javax.net.ssl.HttpsURLConnection;
//
//public class MainActivity extends AppCompatActivity {
//
//    private ImageView imageView;
//    private Button camButton;
//    Bitmap capturedImage;
//    private ProgressDialog progressDialog;
//    URL url;
//
//    private static final int ACTIVITY_REQUEST_CODE = 1000;
//    private static final int PERMISSION_REQUEST_CODE = 2000;
//
//    StringBuffer response;
//    String serverURL = "https://vision.googleapis.com/v1/images:annotate";
//    String responseText;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_main);
//
//        imageView = findViewById(R.id.imageView);
//        camButton = findViewById(R.id.camButton);
//
//        camButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //new GetServerData().execute(serverURL);
//                checkPermissionAndOpenCamera();
//            }
//        });
////        sumbitButton.setOnclickListener(new View. OnClickListener()){
////            @Override
////            public void onClick(View view) {
////
////                new GetServerData().execute(serverURL);
////            }
////        }
//    }
//
//    private void checkPermissionAndOpenCamera() {
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
//            return;
//        }
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//        switch(requestCode){
//            case ACTIVITY_REQUEST_CODE:
//                capturedImage = (Bitmap) data.getExtras().get("data");
//                imageView.setImageBitmap((capturedImage));
//                break;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch(requestCode){
//            case PERMISSION_REQUEST_CODE:
//                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    checkPermissionAndOpenCamera();
//                }
//                break;
//        }
//    }
//
//    class GetServerData extends AsyncTask{
//
//        @Override
//        protected void onPreExecute(){
//            super.onPreExecute();
//            //Show progress dialog
//            progressDialog = new ProgressDialog(MainActivity.this);
//            progressDialog.setMessage("Analyzing Capture");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            return getWebServiceResponseData((String) objects[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//            // Dismiss the progress dialog
//            if (progressDialog.isShowing())
//                progressDialog.dismiss();
//        }
//    }
//
//    protected Void getWebServiceResponseData(String path) {
//
//        try {
//            url = new URL(path + "eloise-reymond-lbzKftE8Xps-unsplash");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(5000);
//            conn.setConnectTimeout(5000);
//            conn.setRequestMethod("GET");
//
//            int responseCode = conn.getResponseCode();
//
//            Log.d("WebService", "Response code: " + responseCode);
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                // Reading response from input Stream
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream()));
//                String output;
//                response = new StringBuffer();
//
//                while ((output = in.readLine()) != null) {
//                    response.append(output);
//                }
//                in.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        responseText = response.toString();
//        Log.i("ID", responseText);
//
//        //COMMENT OUT FROM HERE TO END
//        //Feature feature = new Features();
//        //AnnotateIm
////
////        try {
////
////            jsonResponse = new JSONArray(responseText);
////
////            //changed to 6 to load 6 items first, changes countCheck > 0 to test more recipes
////            for (int i = 0;i< 6;i++ )
////            {
////                String id = "";
////                String mealName = "";
////                String imageURL = "";
////                mealItem = jsonResponse.getJSONObject(i);
////                Integer countCheck = Integer.valueOf(mealItem.getString("missedIngredientCount"));
////                if( countCheck > 0){
////                    id = mealItem.getString("id");
////                    mealName = mealItem.getString("title");
////                    imageURL = mealItem.getString("image");
////                    Log.i("ID: ", id);
////                    Log.i("mealInfo: ", mealName);
////                    Log.i("IMAGE LINK: ", imageURL);
////                    recipes.add(new RecipeRecyclerModel(id, mealName, getBitmapFromURL(imageURL), imageURL));
////                }
////            }
////        } catch (JSONException e) {
////            Log.i("EXCEPTION HERE :", "AAAAAAA");
////            e.printStackTrace();
////        }
//
//        return null;
//    }
//}