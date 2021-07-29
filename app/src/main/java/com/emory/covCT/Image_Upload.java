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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
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
import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private Button predict,load, saliencymap_gen;
    private ImageView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    Bitmap bmp;
    private String covid,noncovid;
    String TAG = "ImageUpload";
    ProgressDialog progressDialog;

    private Interpreter interpreter;
    private Interpreter.Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__upload);

        initViews();
        loadInitInterpreter();
    }

    private void loadInitInterpreter(){
        try{
            options = new Interpreter.Options();
           /* CompatibilityList compatList = new CompatibilityList();

            if(compatList.isDelegateSupportedOnThisDevice()){
                // if the device has a supported GPU, add the GPU delegate
                GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
                GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
                options.addDelegate(gpuDelegate);
            } else {
                // if the GPU is not supported, run on 4 threads*/
                options.setNumThreads(4);
          //  }

            //Loading the model from the assets
           // localModel = new FirebaseCustomRemoteModel.Builder("kjkjkj.dat").build();

          /*  FirebaseModelManager.getInstance().getLatestModelFile(localModel)
                    .addOnCompleteListener(new OnCompleteListener<File>() {
                        @Override
                        public void onComplete(@NonNull Task<File> task) {
                            File modelFile = task.getResult();
                            if (modelFile != null) {
                                interpreter = new Interpreter(modelFile);
                            } else {*/
                                try {
                                    /*File f = new File(getExternalCacheDir()+"/decrypted_model_u2_v3.dat");
                                    InputStream inputStream = new FileInputStream(f);*/
                                    InputStream inputStream = getAssets().open("converted_model_5fold_98_acc.tflite");
                                    byte[] model = new byte[inputStream.available()];
                                    inputStream.read(model);
                                    ByteBuffer buffer = ByteBuffer.allocateDirect(model.length)
                                            .order(ByteOrder.nativeOrder());
                                    buffer.put(model);
                                    interpreter = new Interpreter(buffer,options);
                                    predict.setEnabled(true);

                                    Log.e(TAG,"Interpreter Initialised !");
                                    System.out.println("--------------"+interpreter.getInputTensorCount()+"");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                  /*          }
                        }
                    });*/

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void initViews(){
        predict = findViewById(R.id.inference_button);
        load = findViewById(R.id.upload_button);
        imageView = findViewById(R.id.uploaded_image);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progress);
        saliencymap_gen = findViewById(R.id.saliency_map_gen);

        predict.setEnabled(false);
        progressDialog = new ProgressDialog(Image_Upload.this);
        progressDialog.setTitle("Running Segmentation !");


        //On Click listener for the Load button
        load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((ContextCompat.checkSelfPermission(Image_Upload.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(Image_Upload.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(Image_Upload.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 99);
                    } else {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 999);
                    }
                }
            });

        //On Click Listenerfor the predict button
        predict.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageView.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    bmp = drawable.getBitmap();
                    if(bmp==null){
                        Toast.makeText(Image_Upload.this, "Please Upload Image !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(bmp.getWidth()==bmp.getHeight()) {
                        progressDialog.show();
                        predict.setEnabled(false);
                        load.setEnabled(false);
                        load.setVisibility(View.INVISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                separateLungs(bmp);
                            }
                        }, 1000);

                    }
                    else{
                        Toast.makeText(Image_Upload.this, "Image Not of Equal Dimensions !", Toast.LENGTH_LONG).show();
                    }
                }
            });

        //On click listener for saliencymap button
         saliencymap_gen.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Image_Upload.this,Saliency_Map.class );
                 ByteArrayOutputStream strem = new ByteArrayOutputStream();
                 bmp.compress(Bitmap.CompressFormat.PNG,100,strem);
                 byte[] bytearray = strem.toByteArray();
                 intent.putExtra("image",bytearray);
               //  intent.putExtra("image",bmp);
                 startActivity(intent);
             }
         });

    }

    private void separateLungs(Bitmap bmp){
        new Handler().post(new Runnable(){
            @Override
            public void run(){

                if(bmp==null){
                    Toast.makeText(Image_Upload.this, "No Image Selected !", Toast.LENGTH_SHORT).show();
                    return;
                }

                final Bitmap bmp_test = Bitmap.createScaledBitmap(bmp, 512, 512, true);

                final Mat test_mat =  new Mat();
                Mat test_mat_copy = new Mat();
                Mat final_mat = new Mat();
                Mat thresh = new Mat();
                Mat kernel = Mat.ones(new Size(3,3),CvType.CV_32S);
                Mat kernel_s = Mat.ones(new Size(5,5),CvType.CV_32S);
                Utils.bitmapToMat(bmp_test, test_mat);
                Utils.bitmapToMat(bmp_test,test_mat_copy);
                Utils.bitmapToMat(bmp_test,final_mat);
                Mat gray = new Mat();
                Imgproc.cvtColor(test_mat,gray,Imgproc.COLOR_BGR2GRAY);

                Imgproc.threshold(gray, thresh,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
                Imgproc.morphologyEx(thresh,thresh,Imgproc.MORPH_OPEN,kernel,new Point(0,0),1);
                Imgproc.dilate(thresh,thresh,kernel,new Point(2,2),1);
                Imgproc.erode(thresh,thresh,kernel_s,new Point(3,3),1);
                //Imgproc.dilate(thresh,thresh,kernel,new Point(0,0),2);

                List<MatOfPoint> contours = new ArrayList<>();
                final List<MatOfPoint> contours_sorted = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(thresh,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_NONE);

                for(int i=0;i<contours.size();++i){
                    double area = Imgproc.contourArea(contours.get(i));
                    if(area>10000 && area<70000){
                        contours_sorted.add(contours.get(i));
                    }
                }

                if(contours_sorted.size()>=1){
                    progressDialog.cancel();
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
                    },1200);

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
                    },2200);

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
                            getInferenceNeuralNet(bmp);
                        }
                    },2000);

                }
                else{
                    progressDialog.cancel();
                    Toast.makeText(Image_Upload.this, "Lungs segmentation Unsuccessful !!", Toast.LENGTH_SHORT).show();
                    getInferenceNeuralNet(bmp);
                }
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

    private void getInferenceNeuralNet(Bitmap bitmap){
        final ProgressDialog dialog = new ProgressDialog(Image_Upload.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Generating Inference");
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
                if(a<((int)Double.parseDouble(covid))){
                    progressBar.setProgress(a+1);
                    j.postDelayed(this,5);
                }
                else{
                    dialog.cancel();
                    textView.setText("Results: Covid = "+covid+"\n         Non-Covid = "+noncovid);
                    saliencymap_gen.setVisibility(View.VISIBLE);
                }
            }
        };
        Bitmap bmp_test = bitmap.copy(Bitmap.Config.ARGB_8888, false);
       /* Mat test_mat = new Mat();
        Utils.bitmapToMat(bmp_test, test_mat);*/

        Bitmap finaul = Bitmap.createScaledBitmap(bmp_test, 512, 512, true);

        //  progressBar.setProgress(35);

        //Fitting the final bitmap to the model
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 512 * 512 * 1).order(ByteOrder.nativeOrder());


        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                int px = finaul.getPixel(x, y);

                // Get channel values from the pixel value.
                float r_temp = Color.red(px);

                input.putFloat(r_temp);
            }
        }


        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());

        //Finally running teh Inference of model
        interpreter.run(input, modelOutput);

        Toast.makeText(Image_Upload.this, "Inference Generated !", Toast.LENGTH_SHORT).show();

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();

        probabilities.rewind();
        covid = (probabilities.get(0)*100)+"";
        noncovid = (probabilities.get(1)*100)+"";
                j.post(r);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==111 && resultCode == Activity.RESULT_OK) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Uri selectedImage = data.getData();
           /* try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                Log.d(TAG,"The size of Input stream : "+is.available());
                Bitmap bmp = BitmapFactory.decodeStream(is,null,opt);
                Log.d(TAG,"The height of bitmap : "+bmp.getHeight()+" the width :"+bmp.getWidth());

                int[] pixels = new int[512*512];
                bmp.getPixels(pixels,0,512,0,0,512,512);


                Log.v(TAG,"The value at 351,245 :"+(pixels[324*512+512]&0x000ffff)+"  "+(pixels[323*512+512]&0x000ffff));
               *//* int largest =0,px=0;
                for (int i =0;i<bmp.getWidth();++i){
                    int j=0;
                    for (;j<bmp.getHeight();++j){
                        px = bmp.getPixel(i,j)&0xBB8;
                        if(px>largest){
                            largest = px;
                        }
                    }
                    Log.v(TAG,"The index at "+i+" and "+j+" is: "+px);
                }
                Log.v(TAG,"The largest valeu is :"+largest);*//*

                //  imageView.setImageURI(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
          /*  try{
                File file = new File(data.getData().getPath());
                String selectedpath  = FileP
                byte[] buff = new byte[(int)file.length()];

                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(buff,0,buff.length);
                Log.d(TAG,"The size of bytes is :"+buff.length);
            }
            catch (Exception e){
              Log.d(TAG,e.getMessage());
            }*/

        }
        else if(requestCode==999 && resultCode==Activity.RESULT_OK){
            Uri uri = data.getData();
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
        }
        else{
            Log.e("FFF","OpenCV Loaded Successfully !!");
        }
    }


}
