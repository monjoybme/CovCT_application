package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeOptions extends AppCompatActivity {

    ViewPager viewPager;
    ViewpagerAdapter viewPagerAdapter;
    Button  button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_options);

        viewPager = findViewById(R.id.viewpager);
        button = findViewById(R.id.button);
        viewPagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.add(new LoadAnalysePredictFragment());
        viewPagerAdapter.add(new OptionsFragment());
        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              viewPager.setCurrentItem(1);

            }
        });

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
