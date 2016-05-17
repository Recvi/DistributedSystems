package gr.aueb.cs.ds.dsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import gr.aueb.cs.ds.dsapp.getpoi.SelectArea;
import gr.aueb.cs.ds.dsapp.getpoi.SelectTime;

// Main Activity, for now goes directly to getPoiz activity
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SelectArea.class);
        startActivity(intent);
    }

}
