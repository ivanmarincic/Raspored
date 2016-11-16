package com.idiotnation.raspored.Views;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.idiotnation.raspored.R;

import java.io.File;

public class IntroView extends AppCompatActivity {

    SharedPreferences prefs;
    Button next;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        if(prefs.getInt("DeleteData", -2)==-2){
            prefs.edit().putInt("DeleteData", 1).apply();
            if(new File(getFilesDir() + "/raspored.json").exists()){
                new File(getFilesDir() + "/raspored.json").delete();
            }
            prefs.edit().remove("SpinnerDefault").apply();
        }
        if(prefs.getBoolean("FirstRun", true)){
            setContentView(R.layout.intro_layout);
            next = (Button)findViewById(R.id.next_intro);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs.edit().putBoolean("FirstRun", false).apply();
                    startActivity(new Intent(getApplicationContext(), MainView.class));
                }
            });
        }else {
            startActivity(new Intent(getApplicationContext(), MainView.class));
        }
    }
}
