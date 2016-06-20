package gr.aueb.cs.ds.dsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import gr.aueb.cs.ds.dsapp.getpoi.SelectArea;

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
        Intent intent = new Intent(this, target);
        startActivity(intent);
    }

}
