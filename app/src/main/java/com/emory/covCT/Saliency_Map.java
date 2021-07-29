package com.emory.covCT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.emory.covCT.DataModels.ImageOnly;
import com.emory.covCT.DataModels.SaliencyModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Saliency_Map extends AppCompatActivity {

    ImageView saliency_imae;
    Button generate, save, home;
    SeekBar bar ;
    Bitmap original, saliency;
    CheckBox chech_grad;
    Retrofit mAPIservice;
    ProgressDialog progressDialog;
    String TAG ="Saliency";
    LinearLayout buttons;
    EditText ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saliency__map);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)== PackageManager.PERMISSION_GRANTED){
            checkNetwork();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE},2012);
        }
        /*
        * Here we will gte teh saliency map from the web server
        * The server will return an image (A array) which will be converted to the bitmap and
        * Stored to teh Bitmap named Saliency_map
        *
        * */

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(240, TimeUnit.SECONDS)
                .connectTimeout(240, TimeUnit.SECONDS)
                .writeTimeout(240,TimeUnit.SECONDS)
                .build();

        mAPIservice = new Retrofit.Builder()
                .baseUrl("http://34.135.245.147:5000/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final ProgressDialog dialog = new ProgressDialog(Saliency_Map.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Generating Saliency Map !");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait !");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setProgressNumberFormat(null);

        getBitmap();

        generate = findViewById(R.id.report_generate);
        bar = findViewById(R.id.seekBar);
        chech_grad = findViewById(R.id.check_grad);
        saliency_imae = findViewById(R.id.saliency_map_imageview);
        save = findViewById(R.id.save_saliency);
        home = findViewById(R.id.return_tohome);
        buttons  = findViewById(R.id.second_linear);
        buttons.setVisibility(View.GONE);

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage();
            }
        });

        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                saliency_imae.setImageBitmap(original);
                dialog.cancel();
            }
        },1200);
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
saveImage();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Saliency_Map.this,Image_Upload.class));
                finish();
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

    public void getBitmap(){
        if(getIntent().hasExtra("image")){
            byte[] array = this.getIntent().getByteArrayExtra("image");
            original = BitmapFactory.decodeByteArray(array,0,array.length);

           Toast.makeText(this, "Extra got :"+array.length+" Length ="+original.getWidth(), Toast.LENGTH_SHORT).show();
           // original = (Bitmap)getIntent().getParcelableExtra("image");
        }
    }

    public void sendPost(String imageHash){
        if(mAPIservice!=null){
            Log.d(TAG,"Api has been hit !");
            mAPIservice.create(RetrofitService.class).uploadFile(new ImageOnly(imageHash))
                    .enqueue(new Callback<SaliencyModel>() {
                        @Override
                        public void onResponse(Call<SaliencyModel> call, Response<SaliencyModel> response) {
                            progressDialog.cancel();
                            progressDialog.setTitle("Uploading Image!");
                            Log.d(TAG,"Response from API :"+response.isSuccessful());

                            if(response.isSuccessful()){
                                Log.d(TAG,"Got class prediction :"+response.body().getPredicted_class());
                                byte[] decodedString_without = Base64.decode(response.body().getSaliency_map(), Base64.DEFAULT);
                                Log.d(TAG,"Printed array from response in bytes :"+decodedString_without.length);
                                /*try{
                                    ByteArrayInputStream bis = new ByteArrayInputStream(decodedString_without);
                                    DataInputStream dis = new DataInputStream(bis);
                                    int size = decodedString_without.length;
                                    int[] res = new int[size];
                                    int max = 0;
                                    for(int i =0;i<size;++i){
                                        res[i] = dis.read();
                                        if(res[i]>max){
                                            max=res[i];
                                        }
                                    }
                                    Log.d(TAG,"Length of Recieved Integer array :"+size+" with a maximum value of "+max);

                                    int[] pixels = new int[16*16];

                                    for(int i=0;i<16 * 16 ;){

                                            pixels[i] = Color.rgb(res[i*3+2], res[i*3+1], res[i*3]);
                                    }

                                    //Converting black result to mat
                                    Bitmap result = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
                                    result.setPixels(pixels, 0, 16, 0, 0, 16, 16);

                                    Bitmap resultant = Bitmap.createScaledBitmap(result,512,512,true);
                                    Log.d(TAG, "The height and width of bitmap:"+result.getWidth());
                                    saliency_imae.setImageBitmap(resultant);
                                    //  imageView.setImageBitmap(result);
                                    //Utils.bitmapToMat(result,img);
                                //Bitmap decodedImage_without = BitmapFactory.decodeByteArray(decodedString_without, 0, decodedString_without.length);
                               // saliency_imae.setImageBitmap(decodedImage_without);
                                Toast.makeText(Saliency_Map.this, "Generated !", Toast.LENGTH_LONG).show();}
                                catch(Exception e){
                                    Log.e(TAG,e.toString());
                                }*/
                                Bitmap decodedImage_without = BitmapFactory.decodeByteArray(decodedString_without, 0, decodedString_without.length);
                                Log.d(TAG,"Height and width of bitmap:"+decodedImage_without.getWidth());
                                original=decodedImage_without;
                                saliency_imae.setImageBitmap(decodedImage_without);
                                handleFurtherTask();
                            }
                            else{
                                Toast.makeText(Saliency_Map.this, response.message(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SaliencyModel> call, Throwable t) {
                            progressDialog.setProgress(0);
                            progressDialog.cancel();
                            progressDialog.setTitle("Uploading Image!");
                            Toast.makeText(Saliency_Map.this, t.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else{
            Toast.makeText(this, "Instance null!", Toast.LENGTH_SHORT).show();
            progressDialog.setProgress(0);
            progressDialog.cancel();
            progressDialog.setTitle("Uploading Image!");
        }
    }
    private void sendImage(){

        progressDialog.show();
        progressDialog.setProgress(0);
        progressDialog.setTitle("Uploading Image !");
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                int pr = progressDialog.getProgress();
                if(!(pr>90)){
                    progressDialog.setProgress(pr+5);

                    if(pr>=20 && pr<40){
                        progressDialog.setTitle("Analysing Image");
                    }
                    else if(pr>=40 && pr<75){
                        progressDialog.setTitle("Gradients !");
                    }
                    else if(pr>=75 && pr<90){
                        progressDialog.setTitle("Receiving Maps!");
                    }
                    else if(progressDialog.getProgress()>=90){
                        progressDialog.setTitle("Just few moments!");
                    }

                    h.postDelayed(this,1000);
                }
            }
        },300);

        ByteBuffer input = ByteBuffer.allocateDirect((Integer.SIZE / Byte.SIZE) * 512 * 512).order(ByteOrder.nativeOrder());
        Log.d(TAG, "The size of array initialised :"+input.capacity());
        IntBuffer intbuf= input.asIntBuffer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512*512);
        DataOutputStream dos = new DataOutputStream(baos);


        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                int px = original.getPixel(x, y);

                // Get channel values from the pixel value.
                float r_temp = Color.red(px);
                //input.putInt((int)r_temp);
                intbuf.put((int)r_temp);
                try{
                dos.write((int)r_temp);}
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        byte[] tt = input.array();
        //input.get(tt,0,tt.length);
        Log.d(TAG, "The size of array transfered :"+baos.toByteArray().length);

        // cropImageView.setImageBitmap(cropped);
        //ByteArrayOutputStream output = new ByteArrayOutputStream();
          //      original.compress(Bitmap.CompressFormat.PNG,100,output);
            //    byte[] bytearray =  output.toByteArray();
                String encoded = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
                sendPost(encoded);
    }

    private void checkNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){

        }
        else{
            Toast.makeText(this, "No Internet Connection !", Toast.LENGTH_LONG).show();
        }
    }

    public void handleFurtherTask(){
        buttons.setVisibility(View.VISIBLE);
        generate.setVisibility(View.GONE);
    }

    private void saveImage(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
          progressDialog.show();
            try{
                BitmapDrawable draw = (BitmapDrawable) saliency_imae.getDrawable();
                Bitmap bitmap = draw.getBitmap();
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/covct_heatmaps");
                if(!dir.exists()){
                    dir.mkdir();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                        if(dir.exists()){
                            try{
                                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                                File outFile = new File(dir, fileName);
                                if (outFile.exists())
                                    outFile.delete();
                                FileOutputStream outStream = new FileOutputStream(outFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                outStream.flush();
                                outStream.close();
                                Toast.makeText(Saliency_Map.this, "Saved in Gallery !", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(Uri.fromFile(outFile));
                                sendBroadcast(intent);
                            }
                            catch (Exception e){
                                Toast.makeText(Saliency_Map.this, "in"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(Saliency_Map.this, "Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },3000);
            }
            catch (Exception e){
                if(progressDialog.isShowing()){
                    progressDialog.cancel();
                }
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2011);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       /* if(requestCode==2011 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            saveImage();
        }
        else*/ if(requestCode==2012 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            checkNetwork();
        }
        else {
            Toast.makeText(this, "Permission denied !", Toast.LENGTH_SHORT).show();
        }

    }
}
