package gr.aueb.cs.ds.dsapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class DoCheckin extends Activity {

    ImageView image;
    String imgurKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_checkin);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        imgurKey = SP.getString("imgurkey", "");

        image = (ImageView) findViewById(R.id.taken_image);
        dispatchTakePictureIntent();
    }

    private void postNewCheckin(String url, Location loc) {
        System.out.println("New Checkin Start ###########");

        System.out.println("IMG URL: " + url);
        if (loc != null) {
            System.out.println("Latitude: " + loc.getLatitude());
            System.out.println("Longitude: " + loc.getLongitude());
        }

        System.out.println("New Checkin End ###########");

        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("You successefuly posted a new Checkin, Thank you!")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    @Nullable
    private Location getLoc() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return loc;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);

            new UploadImageTask(imgurKey).execute(imageBitmap);
        } else {
            System.out.println("You are a disgrace to your family!");
            finish();
        }
    }


    private class UploadImageTask extends AsyncTask<Bitmap , Void, String> {

        String apiKey;

        public UploadImageTask(String apiKey) {
            this.apiKey = apiKey;
        }

        protected String doInBackground(Bitmap... imageBitmaps) {
            try {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String imageBase64ed = Base64.encodeToString(byteArray, Base64.DEFAULT);

                URL url = new URL("https://api.imgur.com/3/image");
                HttpsURLConnection client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Authorization", "Client-ID " + apiKey);
                client.setRequestProperty( "Accept-Encoding", "" );
                client.setRequestProperty("Connection", "close");
                client.setDoOutput(true);
                client.setDoOutput(true);
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                writer.write(URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(imageBase64ed, "UTF-8"));
                writer.flush();
                writer.close();

                BufferedReader br=new BufferedReader(new InputStreamReader(client.getInputStream()));
                String response = "";
                String line = "";
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return "false";
            }

        }

        protected void onPostExecute(String response) {
            if (response.equals("false")) {
                System.out.println("You are a disgrace to your family!");
                finish();
            } else {
                try {
                    JSONObject root = new JSONObject(response);
                    JSONObject parent = root.getJSONObject("data");
                    //System.out.println(parent.getString("link"));
                    postNewCheckin(parent.getString("link"), getLoc());
                } catch (JSONException e) {
                    System.out.println("You are a disgrace to your family!");
                    e.printStackTrace();
                    finish();
                }
            }

        }
    }
}
