package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    /*
    Code designed and Written by : ARYAN VERMA
                                   GSOC (Google Summer of Code 2021)
    Mail :                         aryanverma19oct@gmail.com
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        LinearLayout linearLayout = findViewById(R.id.linearlayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1400);
        animationDrawable.setExitFadeDuration(1400);
        animationDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,StoragePermission.class);
                startActivity(intent);
                finish();
            }
        },2800);

    }
}
