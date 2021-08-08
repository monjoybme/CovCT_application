package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.emory.covCT.Adapters.ViewpagerAdapter;

/*
Code designed and Written by : ARYAN VERMA
                               GSOC (Google Summer of Code 2021)
Mail :                         aryanverma19oct@gmail.com
*/
public class HomeOptions extends AppCompatActivity {

    ViewPager viewPager;
    ViewpagerAdapter viewPagerAdapter;
    Button  button;
    ImageView moreOptiosn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_options);

        viewPager = findViewById(R.id.viewpager);
        button = findViewById(R.id.button);
        moreOptiosn = findViewById(R.id.imageView4);

        viewPagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.add(new LoadAnalysePredictFragment());
        viewPagerAdapter.add(new OptionsFragment());
        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);


        button.setOnClickListener(view -> viewPager.setCurrentItem(1));

        moreOptiosn.setOnClickListener(view -> startActivity(new Intent(HomeOptions.this,MoreOptions.class)));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==1){
                    button.setVisibility(View.INVISIBLE);
                }
                else{
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }
}
