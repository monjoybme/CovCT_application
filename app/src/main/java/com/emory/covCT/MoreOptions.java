package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreOptions extends AppCompatActivity {

    ImageView close;
    TextView share,faq,privacy,uni,developer;

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
        privacy = findViewById(R.id.privacy_policy);
        uni = findViewById(R.id.moreapp);
        developer = findViewById(R.id.development_info);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        uni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
