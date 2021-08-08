package com.emory.covCT;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
Code designed and Written by : ARYAN VERMA
                               GSOC (Google Summer of Code 2021)
Mail :                         aryanverma19oct@gmail.com
*/
public class Saliency_Map extends AppCompatActivity {

    ImageView saliency_imae;
    Button generate, save, home;
    SeekBar bar ;
    TextView label;
    Bitmap original, mixed_bit, heatmap;
    CheckBox chech_grad;
    ProgressDialog progressDialog,dialog1;
    String TAG ="Saliency";
    LinearLayout buttons;
    String filename;
    Mat mask;
    int[] coordinates = new int[4];
    boolean run=true;
    float[] rawActivations, input_image;
    int ts1=0,ts2=0,ts3=0,ts4=0, ts5=0,ts6=0,ts7=0,ts8=0;
    private Interpreter interpreter,interpreter2,interpreter3,interpreter4, interpreter5, interpreter6, interpreter7, interpreter8, interpreter_saliency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saliency__map);

        loadInitInterpreter();

        //Dialog for Bitmap and other recievings from previous activity
        final ProgressDialog dialog = new ProgressDialog(Saliency_Map.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Please wait !");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        //dialog for saliency operations
        dialog1 = new ProgressDialog(Saliency_Map.this);
        dialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog1.setTitle("Generating Heatmap !!");
        dialog1.setMessage("This may take few Seconds (60-70), depending on your phone's speed.");
        //dialog1.show();
        dialog1.setCancelable(false);

        //Dialog for saving images
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Imposed Heatmap !");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mask = new Mat(512,512,CvType.CV_8U);
        getBitmap();

        generate = findViewById(R.id.report_generate);
        bar = findViewById(R.id.seekBar);
        chech_grad = findViewById(R.id.check_grad);
        saliency_imae = findViewById(R.id.saliency_map_imageview);
        save = findViewById(R.id.save_saliency);
        home = findViewById(R.id.return_tohome);
        buttons  = findViewById(R.id.second_linear);
        buttons.setVisibility(View.GONE);
        generate.setEnabled(false);
        label = findViewById(R.id.label);
        label.setVisibility(View.GONE);
        bar.setVisibility(View.GONE);
        chech_grad.setVisibility(View.GONE);


        Handler showmat = new Handler(Looper.getMainLooper());
        Runnable showit = () -> {
            if(dialog.isShowing()){
                dialog.cancel();
            }
            saliency_imae.setImageBitmap(mixed_bit);
            Toast.makeText(this, "Imposed Heatmap !", Toast.LENGTH_SHORT).show();
            handleFurtherTask();
        };

        chech_grad.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.d(TAG, "Changed with value "+b );
            if(b){
                if(mask==null){
                    Toast.makeText(Saliency_Map.this, "Can't Segment Lungs!", Toast.LENGTH_SHORT).show();
                    chech_grad.setChecked(false);
                }
                else{
                    Mat heat_small = new Mat(16,16,CvType.CV_32F);
                    Utils.bitmapToMat(heatmap,heat_small);
                    //  heatmap.recycle();
                    Log.d(TAG, " Generated heatmap to mat");

                    Mat heat = new Mat(512,512,CvType.CV_32F);
                    Imgproc.resize(heat_small,heat,new Size(512,512));
                    //  heat_small.release();

                    Mat original_lung = new Mat(512,512, CvType.CV_32F);
                    Utils.bitmapToMat(original,original_lung);
                    // original.recycle();

                    Mat maksed_heat = new Mat(512,512, CvType.CV_32F);
                    heat.copyTo(maksed_heat,mask);
                    //  heat.release();

                    Mat maksed_original = new Mat(512,512, CvType.CV_32F);
                    original_lung.copyTo(maksed_original,mask);
                    //original_lung.release();
                    //  mask.release();

                    Mat mixed = new Mat(512,512,CvType.CV_32F);

                    Core.addWeighted(maksed_original,1.2,maksed_heat,0.5,0,mixed);

                    Mat sub = mixed.submat(coordinates[0],coordinates[2],coordinates[1],coordinates[3]);
                    mixed_bit = Bitmap.createBitmap(sub.cols(), sub.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(sub,mixed_bit);
                    dialog.show();

                    showmat.postDelayed(showit,1500);
                }
            }
            else{
                Mat heat_small = new Mat(16,16,CvType.CV_32F);
                Utils.bitmapToMat(heatmap,heat_small);
                //  heatmap.recycle();
                Mat heat = new Mat(512,512,CvType.CV_32F);
                Imgproc.resize(heat_small,heat,new Size(512,512));
                //  heat_small.release();

                Mat original_lung = new Mat(512,512, CvType.CV_32F);
                Utils.bitmapToMat(original,original_lung);
                // original.recycle();
                //  mask.release();

                Mat mixed = new Mat(512,512,CvType.CV_32F);
                Core.addWeighted(original_lung,1.2,heat,0.5,0,mixed);

                //  Mat sub = mixed.submat(coordinates[0],coordinates[2],coordinates[1],coordinates[3]);
                mixed_bit = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mixed,mixed_bit);

                dialog.show();
                showmat.postDelayed(showit,1000);
            }
        });

        generate.setOnClickListener(view -> {
            generate.setEnabled(false);
            dialog1.show();
            ProcessImageSaliency();
            dialog1.setProgress(3);
        });

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

        save.setOnClickListener(view -> saveImage());

        home.setOnClickListener(view -> {
            startActivity(new Intent(Saliency_Map.this,Image_Upload.class));
            finish();
        });

    }

    public void getBitmap(){
        Thread t = new Thread(() -> {
            if(getIntent().hasExtra("image")){
                saliency_imae.setImageURI(Uri.parse(getIntent().getStringExtra("image")));
                saliency_imae.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) saliency_imae.getDrawable();
                original = drawable.getBitmap();
                new Handler(Looper.getMainLooper()).post(() -> generate.setEnabled(true));
            }
            if(getIntent().hasExtra("mask")){
                if(getIntent().getStringExtra("mask")!=null){
                    File file = new File(getIntent().getStringExtra("mask"));
                    int size = (int)file.length();
                    byte[] bytes = new byte[size];
                    try{
                        BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                        is.read(bytes,0,bytes.length);
                        is.close();
                        mask.put(0,0,bytes);

                        coordinates = getIntent().getIntArrayExtra("coordinates");
                        Log.d(TAG, "Coordinates from previouspage :"+coordinates[0]+" "+coordinates[1]);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if(getIntent().hasExtra("filename")){
                filename = getIntent().getStringExtra("filename");
            }

        });
        t.start();
    }

    private void loadInitInterpreter(){
       LoadInterpreters interpreters = new LoadInterpreters();
       interpreters.execute();
    }

    public void ProcessImageSaliency(){
        //This thread generates the Raw Activations and passes them to process() function
        Thread generators = new Thread(() -> {
            rawActivations = new float[16*16*320];
            Log.d(TAG,"Generating the activations !!");
            //Generate the buffer so that it can be put to model
            ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
            input_image = new float[512*512];
            for (int y = 0; y < 512; y++) {
                for (int x = 0; x < 512; x++) {
                    int px = original.getPixel(x, y);

                    // Get channel values from the pixel value.
                    float r_temp = Color.red(px);
                    input.putFloat(r_temp);
                    input_image[y*512+x]=r_temp;
                }
            }
            dialog1.setProgress(8);
            input.clear();
            int bufferSize = 16 * 16 * 320 * Float.SIZE / Byte.SIZE;
            ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
            interpreter_saliency.run(input, modelOutput);
            interpreter_saliency.close();
            modelOutput.rewind();

            FloatBuffer probabilities = modelOutput.asFloatBuffer();
            modelOutput.clear();
            probabilities.rewind();
            probabilities.get(rawActivations,0,16*16*320);
            probabilities.clear();

            INDArray outputs = Nd4j.create(rawActivations).reshape(new int[]{16,16,320});
            dialog1.setProgress(11);
            INDArray permuted = outputs.permute(new int[]{2,0,1});
            rawActivations = permuted.reshape(new int[]{320*16*16}).toFloatVector();
            permuted.close();

            Log.d(TAG,"Activations generated with the size of float array as :"+rawActivations.length);
            dialog1.setProgress(17);
            process(rawActivations);
        });
        generators.start();
    }

    public void process(float[] rawActivations){

        //The array containg the softmax outputs of teh masked images
        float[] softmax = new float[40];

        Thread t1 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=0;i<40;i=i+8){
                if(run){
                    float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                    small.put(0,0,image);
                    Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                    big.get(0,0,processed);
                    //Data came in processed array
                    INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                    //Normalize the activation map
                    INDArray normalized = normalize2x2ArrayI(ndimage);

                    //create the mask of this image
                    INDArray masked_input = createMaskedInput(normalized);

                    //Predict on masked image and get the softmax
                    float softmax_output = predictOnMaskedImages(masked_input);
                    softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);
                }
                else{
                    break;
                }
            }
            interpreter.close();
            ts1=1;
        });

        Thread t2 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=40;i<80;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages1(masked_input);
                //softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);

                }
                else{
                break;
            }
            }
            interpreter2.close();
            ts2=1;
        });

        Thread t3 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=80;i<120;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages2(masked_input);
               // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);

                }
                else{
                break;
            }
            }
            interpreter3.close();
            ts3=1;
        });

        Thread t4 = new Thread(() -> {

            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=120;i<160;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages3(masked_input);
               // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;

                    dialog1.setProgress(dialog1.getProgress()+2);
                }
                else{
                    break;
                }
            }
            interpreter4.close();
            ts4=1;
        });

        Thread t5 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=160;i<200;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages4(masked_input);
                // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);


                }
                else{
                    break;
                }
            }
            interpreter5.close();
            ts5=1;
        });

        Thread t6 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=200;i<240;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages5(masked_input);
                // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);

                }
                else{
                    break;
                }
            }
            interpreter6.close();
            ts6=1;
        });

        Thread t7 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=240;i<280;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages6(masked_input);
                // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);

                }
                else{
                    break;
                }
            }
            interpreter7.close();
            ts7=1;
        });

        Thread t8 = new Thread(() -> {
            Mat big = new Mat(512,512,CvType.CV_32F);
            Mat small = new Mat(16,16,CvType.CV_32F);
            float[] processed = new float[512*512];
            for (int i=280;i<320;i=i+8){
                if(run){
                // float[] image = getImage(rawActivations);
                float[] image = Arrays.copyOfRange(rawActivations,(i*256),((i+1)*256));
                small.put(0,0,image);
                Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_LINEAR);
                big.get(0,0,processed);
                //Data came in processed array
                INDArray ndimage = Nd4j.create(processed).reshape(new int[]{512,512});

                //Normalize the activation map
                INDArray normalized = normalize2x2ArrayI(ndimage);

                //create the mask of this image
                INDArray masked_input = createMaskedInput(normalized);

                //Predict on masked image and get the softmax
                float softmax_output = predictOnMaskedImages7(masked_input);
                // softmax.add(softmax_output);
                softmax[i/8]=softmax_output;
                    dialog1.setProgress(dialog1.getProgress()+2);
                }
                else{
                    break;
                }
            }
            interpreter8.close();
            ts8=1;
        });

       t1.start();
       t2.start();
       t3.start();
       t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();

       Handler afterProcess = new Handler(Looper.getMainLooper());
       Runnable afterProcessRunnable = () -> {
           if(run){
               dialog1.setProgress(dialog1.getProgress()+3);
               float[] weighted_map = new float[16*16];
               for(int i=0;i<256;++i){
                   float sum=0;
                   for (int j=0;j<40;++j){
                       sum = sum + rawActivations[j*8*256+i]*softmax[j];
                   }
                   if(sum>0){
                       weighted_map[i] = sum;
                   }
               }

               float[] normalized = normalizeFloat(weighted_map);
                heatmap = generateHeatmap(normalized);
               Log.d(TAG, "Heatmap generated finally :"+heatmap.getWidth()+" "+heatmap.getHeight());
               saliency_imae.setImageBitmap(heatmap);

               if(mask!=null){
                   chech_grad.setChecked(true);
               }
               else{
                   chech_grad.setChecked(false);

               }
               dialog1.cancel();
           }
       };

       Handler checker = new Handler(Looper.getMainLooper());
       Runnable checkerRunnable = new Runnable() {
            @Override
            public void run() {
                if(run) {
                    Log.d(TAG, "Checker Runnable called with states " + ts1 + ts2 + ts3 + ts4 + ts5 + ts6 + ts7 + ts8);
                    if (ts1 == 1 && ts2 == 1 && ts3 == 1 && ts4 == 1 && ts5 == 1 && ts6 == 1 && ts7 == 1 && ts8 == 1) {
                        afterProcess.post(afterProcessRunnable);
                    } else {
                        checker.postDelayed(this, 10000);
                    }
                }
            }
        };

        checker.postDelayed(checkerRunnable,60000);
    }

    public float[] normalizeFloat(float[] array){
        float max = max(array);
        for (int i=0;i<array.length;++i){
            //array[i] = (array[i]-min)/(max-min);
            array[i] = array[i]/max;
        }
        return array;
    }

    public float max(float[] array){
        float ret = array[0];
        for (float v : array) {
            ret = Math.max(ret, v);
        }
        return ret;
    }

    private INDArray normalize2x2ArrayI(INDArray array) {
        double currMax = array.maxNumber().doubleValue();
        double currMin = array.minNumber().doubleValue();

        // array.subi(currMin);
        array.divi(currMax - currMin);
        return array;
    }

    public INDArray createMaskedInput(INDArray normalised){
        INDArray input_image_ = Nd4j.create(input_image).reshape(new int[]{512,512});
        return input_image_.mul(normalised);
    }

    public float predictOnMaskedImages(INDArray masked_input){

       // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }
        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages1(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter2.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages2(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter3.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages3(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter4.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages4(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512 ).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter5.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages5(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter6.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages6(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter7.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }
    public float predictOnMaskedImages7(INDArray masked_input){

        // float[][] input_image = masked_input.toFloatMatrix();
        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512 ).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                input.putFloat(masked_input.getFloat(y*512+x));
            }
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //Finally running teh Inference of model
        interpreter8.run(input, modelOutput);
        input.clear();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        modelOutput.clear();
        probabilities.rewind();
        return probabilities.get(0);
    }

    public Bitmap generateHeatmap(float[] normalized){
        int[] gradientColors = Gradients.GRADIENT_BLUE_TO_RED;
        Bitmap heatmap = Bitmap.createBitmap(16,16, Bitmap.Config.ARGB_8888);

        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                double normVal = normalized[row*16+col];
                int colorIndex = (int) Math.floor(normVal * (gradientColors.length - 1));

                // Limit it on the highest color
                // - if we're not normalizing the heatmap then this can cause IndexOutOfBoundsException otherwise
                // E.g., colorIndex = 610 with 500 gradientColors - colorIndex is now 499.
                colorIndex = Math.min(colorIndex, gradientColors.length - 1);

                int color = gradientColors[colorIndex];
                heatmap.setPixel(col,row,color);

            }
        }
        Mat small = new Mat(16,16,CvType.CV_32F);
        Mat big = new Mat(512,512,CvType.CV_32F);
        Bitmap returning = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);

        Utils.bitmapToMat(heatmap,small);
        Imgproc.resize(small,big,new Size(512,512),0,0,Imgproc.INTER_AREA);
        Utils.matToBitmap(big,returning);
        return heatmap;
    }

    public void handleFurtherTask(){
        label.setVisibility(View.VISIBLE);
        bar.setVisibility(View.VISIBLE);
        chech_grad.setVisibility(View.VISIBLE);
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
                new Handler().postDelayed(() -> {
                    progressDialog.cancel();
                    if(dir.exists()){
                        try{
                            String fileName1 = filename+".jpg";
                            File outFile = new File(dir, fileName1);
                            if (outFile.exists())
                                outFile.delete();
                            FileOutputStream outStream = new FileOutputStream(outFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                            outStream.flush();
                            outStream.close();
                            Toast.makeText(Saliency_Map.this, "Saved at : "+dir.getAbsolutePath()+"/"+fileName1, Toast.LENGTH_LONG).show();
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

    public void setHue(int hue){
        Bitmap bmp32 = mixed_bit.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Mat hsv = new Mat();
        Utils.bitmapToMat(bmp32, mat);
        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);
        List<Mat> hsvlist = new ArrayList<>();
        Core.split(hsv,hsvlist);

        Mat hue_channel = hsvlist.get(0);


        Core.add(hue_channel,new Scalar(hue),hue_channel);


        List<Mat> new_hsvlist = new ArrayList<>();
        new_hsvlist.add(hue_channel);
        new_hsvlist.add(hsvlist.get(1));
        new_hsvlist.add(hsvlist.get(2));

        Core.merge(new_hsvlist,mat);
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_HSV2BGR);

        Utils.matToBitmap(mat,bmp32);
        saliency_imae.setImageBitmap(bmp32);
    }

    public class LoadInterpreters extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Interpreter.Options options, options_saliency;
                options = new Interpreter.Options();
                options_saliency = new Interpreter.Options();
                options.setNumThreads(7);
                options_saliency.setNumThreads(7);
                try {

                    InputStream inputStream = getAssets().open("accurate99.tflite");
                    byte[] model = new byte[inputStream.available()];
                    inputStream.read(model);
                    inputStream.close();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(model.length)
                            .order(ByteOrder.nativeOrder());
                    buffer.put(model);

                    interpreter = new Interpreter(buffer,options);
                    interpreter2 = new Interpreter(buffer,options);
                    interpreter3 = new Interpreter(buffer,options);
                    interpreter4 = new Interpreter(buffer,options);
                    interpreter5 = new Interpreter(buffer,options);
                    interpreter6 = new Interpreter(buffer,options);
                    interpreter7 = new Interpreter(buffer,options);
                    interpreter8 = new Interpreter(buffer,options);
                    buffer.clear();

                    Log.e(TAG,"Interpreter Initialised !");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    InputStream inputStream1 = getAssets().open("only_activations.tflite");
                    byte[] model1 = new byte[inputStream1.available()];
                    inputStream1.read(model1);
                    ByteBuffer buffer = ByteBuffer.allocateDirect(model1.length)
                            .order(ByteOrder.nativeOrder());
                    buffer.put(model1);
                    interpreter_saliency = new Interpreter(buffer,options_saliency);
                    buffer.clear();
                    // predict.setEnabled(true);
                    Log.e(TAG,"Interpreter Initialised for saliency!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        run=false;
        dialog1.cancel();
    }
}
