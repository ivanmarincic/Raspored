package com.idiotnation.raspored;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class IntroView extends AppCompatActivity {

    SharedPreferences prefs;
    Button next;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
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
