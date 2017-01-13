package com.idiotnation.raspored.Views;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.io.File;

public class IntroView extends AppCompatActivity {

    SharedPreferences prefs;
    Button next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        if (prefs.getInt("DeleteData", -2) == -2) {
            prefs.edit().putInt("DeleteData", 1).apply();
            if (new File(getFilesDir() + "/raspored.json").exists()) {
                new File(getFilesDir() + "/raspored.json").delete();
            }
            prefs.edit().remove("SpinnerDefault").apply();
        }
        if (prefs.getInt(getResources().getResourceName(R.color.widgetBackgroundColor), 665566) == 665566) {
            prefs.edit().putInt(getResources().getResourceName(R.color.widgetBackgroundColor), Utils.getColor(R.color.lessonsBackgroundColor, getApplicationContext())).apply();
            prefs.edit().putInt(getResources().getResourceName(R.color.widgetTextColorPrimary), Utils.getColor(R.color.lessonsTextColorPrimary, getApplicationContext())).apply();
        }
        if (prefs.getInt(getResources().getResourceName(R.color.textColorSecondary), 1236124) == 1236124) {
            int color = Utils.getColor(R.color.textColorPrimary, getApplicationContext());
            int secondary = Color.argb(178, Color.red(color), Color.green(color), Color.blue(color));
            int disabled = Color.argb(127, Color.red(color), Color.green(color), Color.blue(color));
            prefs.edit().putInt(getResources().getResourceName(R.color.textColorSecondary), secondary).apply();
            prefs.edit().putInt(getResources().getResourceName(R.color.textColorDisabled), disabled).apply();
        }
        if (prefs.getBoolean("FirstRun", true)) {
            setContentView(R.layout.intro_layout);
            next = (Button) findViewById(R.id.next_intro);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs.edit().putBoolean("FirstRun", false).apply();
                    startActivity(new Intent(getApplicationContext(), MainView.class));
                }
            });
        } else {
            startActivity(new Intent(getApplicationContext(), MainView.class));
        }
    }
}
