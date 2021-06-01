package com.emory.covCT;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Saliency_Map extends AppCompatActivity {

    ImageView saliency_imae;
    Button generate;
    SeekBar bar ;
    Bitmap original;
    CheckBox chech_grad;
    private Bitmap Saliency_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saliency__map);


        /*
        * Here we will gte teh saliency map from the web server
        * The server will return an image (A array) which will be converted to the bitmap and
        * Stored to teh Bitmap named Saliency_map
        * */


        final ProgressDialog dialog = new ProgressDialog(Saliency_Map.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Generating Saliency Map !");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        generate = findViewById(R.id.report_generate);
        bar = findViewById(R.id.seekBar);
        chech_grad = findViewById(R.id.check_grad);
        saliency_imae = findViewById(R.id.saliency_map_imageview);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Saliency_Map.this,Image_Upload.class));

                    }                });

            }
        });

        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                saliency_imae.setImageBitmap(Saliency_map);
                dialog.cancel();
                BitmapDrawable drawable = (BitmapDrawable) saliency_imae.getDrawable();
                original = drawable.getBitmap();
            }
        },2000);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                saliency_imae.setImageResource(R.drawable.overlapped25);
            }
        },3500);*/
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setHue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void setHue(int hue){
        Bitmap bmp32 = original.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Mat hsv = new Mat();
        Utils.bitmapToMat(bmp32, mat);
        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);
        List<Mat> hsvlist = new ArrayList<>();
        Core.split(hsv,hsvlist);

        Mat hue_channel = hsvlist.get(0);

        if(chech_grad.isChecked()){
            hue_channel.setTo(new Scalar(hue));
        }
        else{
            Core.add(hue_channel,new Scalar(hue),hue_channel);

        }

        List<Mat> new_hsvlist = new ArrayList<>();
        new_hsvlist.add(hue_channel);
        new_hsvlist.add(hsvlist.get(1));
        new_hsvlist.add(hsvlist.get(2));

        Core.merge(new_hsvlist,mat);
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_HSV2BGR);

        Utils.matToBitmap(mat,bmp32);
        saliency_imae.setImageBitmap(bmp32);
    }
}
