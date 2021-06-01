package com.emory.covCT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Image_Upload extends AppCompatActivity {

    private Button predict,load;
    private ImageView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    private String covid,noncovid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__upload);

        initViews();
    }

    public void initViews(){
        predict = findViewById(R.id.inference_button);
        load = findViewById(R.id.upload_button);
        imageView = findViewById(R.id.uploaded_image);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progress);

        //On Click listener for the Load button
        {
            load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((ContextCompat.checkSelfPermission(Image_Upload.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(Image_Upload.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(Image_Upload.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 99);
                    } else {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 111);
                    }
                }
            });
        }
        //On Click Listenerfor the predict button

            predict.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   separateLungs();
                }
            });

    }

    private void separateLungs(){
        predict.setEnabled(false);
        load.setEnabled(false);
        new Handler().post(new Runnable(){
            @Override
            public void run(){
                imageView.invalidate();

                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                final Bitmap bmp = drawable.getBitmap();
                final Bitmap bmp_test = Bitmap.createScaledBitmap(bmp, 512, 512, true);

                final Mat test_mat =  new Mat();
                Mat test_mat_copy = new Mat();
                Mat final_mat = new Mat();


                Mat thresh = new Mat();
                Mat kernel = Mat.ones(new Size(3,3),CvType.CV_32S);
                Utils.bitmapToMat(bmp_test, test_mat);
                Utils.bitmapToMat(bmp_test,test_mat_copy);
                Utils.bitmapToMat(bmp_test,final_mat);
                Mat gray = new Mat();
                Imgproc.cvtColor(test_mat,gray,Imgproc.COLOR_BGR2GRAY);

                Imgproc.threshold(gray, thresh,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
                Imgproc.morphologyEx(thresh,thresh,Imgproc.MORPH_OPEN,kernel,new Point(0,0),1);
                Imgproc.dilate(thresh,thresh,kernel,new Point(0,0),2);

                List<MatOfPoint> contours = new ArrayList<>();
                final List<MatOfPoint> contours_sorted = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(thresh,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_NONE);

                for(int i=0;i<contours.size();++i){
                    double area = Imgproc.contourArea(contours.get(i));
                    if(area>10000 && area<40000){
                        contours_sorted.add(contours.get(i));
                    }
                }

                //Display image with vacant contour
                Mat lungs = Mat.zeros(new Size(512,512),CvType.CV_32S);
                Imgproc.drawContours(test_mat,contours_sorted,-1,new Scalar(0,255,0),3);

                final Bitmap final_ = Bitmap.createBitmap(test_mat.cols(), test_mat.rows(), Bitmap.Config.ARGB_8888);

                Utils.matToBitmap(test_mat, final_);
                Toast.makeText(Image_Upload.this, "Contours detected for lungs!", Toast.LENGTH_SHORT).show();
                imageView.setImageBitmap(final_);

                //Display image with Filled contour
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Imgproc.drawContours(test_mat,contours_sorted,-1,new Scalar(0,255,0),-1);

                        Utils.matToBitmap(test_mat, final_);
                        imageView.setImageBitmap(final_);

                    }
                },1500);

                //Segmenting the Lungs from the main image
                Mat mask = Mat.zeros(new Size(512,512),CvType.CV_8U);
                Imgproc.drawContours(mask,contours_sorted,-1,new Scalar(255),-1);
                final Mat final_one = new Mat();
                test_mat_copy.copyTo(final_one,mask);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Image_Upload.this, "Lungs Segmented Successfully !", Toast.LENGTH_SHORT).show();
                        Utils.matToBitmap(final_one, final_);
                        imageView.setImageBitmap(final_);
                    }
                },3400);

                //Now to enlarge the Cropped region of segmented lungs
                List<Integer> x = new ArrayList<>();
                List<Integer> y = new ArrayList<>();

                for(int i=0;i<512;++i){
                    for(int j=0;j<512;++j){
                        if(mask.get(i,j)[0]==255){
                            x.add(i);
                            y.add(j);
                        }
                    }
                }

                int topy = Collections.min(y);
                int topx = Collections.min(x);
                int bottomy = Collections.max(y);
                int bottomx = Collections.max(x);

                Mat sub = final_one.submat(topx,bottomx,topy,bottomy);
                final Bitmap final_cropped = Bitmap.createBitmap(sub.cols(), sub.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(sub, final_cropped);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(final_cropped);
                        getInference(bmp_test);
                    }
                },4200);

            }
        });
    }

    private void openfile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try{
            startActivityForResult(Intent.createChooser(intent,"Select a file to upload"),999);

        }
        catch (ActivityNotFoundException e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void getInference(Bitmap bitmap){
        final ProgressDialog dialog = new ProgressDialog(Image_Upload.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Generating Inference");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        predict.setVisibility(View.GONE);
        load.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        // The server or the domestic deployed model will be used
        //to generate teh inference for teh given image
        //The results would be rendered to :
        //covid : The covid percentage
        //non-covid : The non covid percentage


        final Handler j = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                int a = progressBar.getProgress();
                if(a<Integer.parseInt(covid)){
                    progressBar.setProgress(a+1);
                    j.postDelayed(this,5);
                }
                else{
                    textView.setText("Results: Covid = "+covid+"\n         Non-Covid = "+noncovid);
                }
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
                Toast.makeText(Image_Upload.this, "Inference Generated !", Toast.LENGTH_SHORT).show();
                j.post(r);

            }
        },2400);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               Intent intent = new Intent(Image_Upload.this,Saliency_Map.class);
               startActivity(intent);
            }
        },5000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==111 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                imageView.setImageURI(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==999 && resultCode==Activity.RESULT_OK){
            Uri uri = data.getData();
            Toast.makeText(Image_Upload.this,"Got uri:"+uri,Toast.LENGTH_SHORT).show();
            imageView.setImageURI(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 111);
            }
            else
            {
                Toast.makeText(this, "Read and Write permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!OpenCVLoader.initDebug()){
            Log.e("FFF","Opencv not loaded!!!");
        }
        else{
            Log.d("FFF","OpenCV Loaded Successfully !!");
        }
    }
}
