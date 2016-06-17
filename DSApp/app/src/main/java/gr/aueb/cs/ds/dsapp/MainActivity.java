package gr.aueb.cs.ds.dsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import gr.aueb.cs.ds.dsapp.getpoi.SelectArea;


// Main Activity, for now goes directly to getPoiz activity
public class MainActivity extends Activity {

    ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpButtons();
    }

    private void setUpButtons() {
        buttons = new ArrayList<Button>();

        Button getPois = (Button) findViewById(R.id.go_to_pick);
        getPois.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToActivity(SelectArea.class);
            }
        });

        Button settings = (Button) findViewById(R.id.go_to_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToActivity(SettingsActivity.class);
            }
        });

        Button insert = (Button) findViewById(R.id.go_to_insert);
        insert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToActivity(DoCheckin.class);
            }
        });

        buttons.add(getPois);
        buttons.add(settings);
        buttons.add(insert);
    }

    private void goToActivity(Class target) {
        System.out.print("\n\n\n\n\n\n\n\nyolo\n\n\n\n\n\n");
        Intent intent = new Intent(this, target);
        startActivity(intent);
    }

}
