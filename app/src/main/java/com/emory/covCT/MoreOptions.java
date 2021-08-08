package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
/*
Code designed and Written by : ARYAN VERMA
                               GSOC (Google Summer of Code 2021)
Mail :                         aryanverma19oct@gmail.com
*/
public class MoreOptions extends AppCompatActivity {

    ImageView close;
    TextView share,faq,developer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_options);

        initViews();
    }

    public void initViews(){
        close = findViewById(R.id.cut);
        share = findViewById(R.id.share);
        faq = findViewById(R.id.rateNow);
        developer = findViewById(R.id.development_info);

        close.setOnClickListener(view -> finish());

        share.setOnClickListener(view -> {

        });

        faq.setOnClickListener(view -> {

        });


        developer.setOnClickListener(view -> {

        });
    }
}
