package com.emory.covCT;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Image_Upload extends AppCompatActivity {

    private Button predict,load, saliencymap_gen;
    private ImageView imageView;
    private TextView textView, inputText;
    private ProgressBar progressBar;
    Bitmap bmp;
    private String covid,noncovid;
    String TAG = "ImageUpload";
    ProgressDialog progressDialog;
    Mat mask;
    boolean reached = false;
    private Interpreter interpreter;
    private Interpreter.Options options;
    Uri uri;
    int topx,topy,bottomy,bottomx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__upload);
        initViews();
        loadInitInterpreter();
    }

    private void loadInitInterpreter(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    options = new Interpreter.Options();
                    CompatibilityList compatList = new CompatibilityList();
                    options.setNumThreads(7);

                    try {
                        InputStream inputStream = getAssets().open("accurate99.tflite");
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


                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
       t.start();
    }

    public void initViews(){
        predict = findViewById(R.id.inference_button);
        load = findViewById(R.id.upload_button);
        imageView = findViewById(R.id.uploaded_image);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progress);
        saliencymap_gen = findViewById(R.id.saliency_map_gen);
        inputText = findViewById(R.id.inputTextview);
        predict.setEnabled(false);

        progressDialog = new ProgressDialog(Image_Upload.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Running Segmentation !");
        progressDialog.setCancelable(false);

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
                    if(bmp.getWidth()==bmp.getHeight() ) {
                        if(bmp.getHeight()==512){
                            progressDialog.show();
                            inputText.setVisibility(View.GONE);
                            predict.setEnabled(false);
                            load.setEnabled(false);
                            load.setVisibility(View.INVISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    separateLungs();
                                }
                            }, 300);
                        }
                        else{
                            Toast.makeText(Image_Upload.this, "Image must be 512 X 512", Toast.LENGTH_SHORT).show();
                        }
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
                 intent.putExtra("image",uri.toString());

                 byte[] mask_buffer = new byte[512*512*(Integer.SIZE/Byte.SIZE)];
                 if(mask!=null){
                    try{
                        mask.get(0,0,mask_buffer);
                        File cah = Environment.getExternalStorageDirectory();
                        File dir = new File(cah.getAbsolutePath()+"/covct/temp");
                        if(!dir.exists()){
                            dir.mkdirs();
                        }
                        File document = new File(dir,"mask.dat");
                        if(document.exists()){
                            document.delete();
                        }
                        FileOutputStream fos = new FileOutputStream(document.getPath());
                        fos.write(mask_buffer);
                        fos.close();
                        intent.putExtra("mask",document.getPath());
                        intent.putExtra("coordinates",new int[]{topx,topy,bottomx,bottomy});
                        Log.d(TAG,"The file stored at : "+document.getPath());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                 }
               //  intent.putExtra("image",bmp);

                 startActivity(intent);
                 finish();
             }
         });

    }

    private void separateLungs(){
                if(bmp==null){
                    Toast.makeText(Image_Upload.this, "No Image Selected !", Toast.LENGTH_SHORT).show();
                    return;
                }

                final Bitmap bmp_test = Bitmap.createScaledBitmap(bmp, 512, 512, true);

                Mat test_mat =  new Mat();

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
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();

                            //Display image with vacant contour
                            // Mat lungs = Mat.zeros(new Size(512,512),CvType.CV_32S);
                            Imgproc.drawContours(test_mat,contours_sorted,-1,new Scalar(0,255,0),3);
                            Bitmap final_ = Bitmap.createBitmap(test_mat.cols(), test_mat.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(test_mat, final_);
                           new Handler(Looper.getMainLooper()).post(new Runnable() {
                               @Override
                               public void run() {
                                   Toast.makeText(Image_Upload.this, "Contours detected for lungs!", Toast.LENGTH_SHORT).show();
                                   imageView.setImageBitmap(final_);
                               }
                           });

                            //Display image with Filled contour

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Imgproc.drawContours(test_mat,contours_sorted,-1,new Scalar(0,255,0),-1);
                                    Utils.matToBitmap(test_mat, final_);
                                    imageView.setImageBitmap(final_);
                                }
                            },1000);

                            //Segmenting the Lungs from the main image
                            mask = Mat.zeros(new Size(512,512),CvType.CV_8U);
                            Imgproc.drawContours(mask,contours_sorted,-1,new Scalar(255),-1);
                            final Mat final_one = new Mat();
                            test_mat_copy.copyTo(final_one,mask);


                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!reached){
                                        Utils.matToBitmap(final_one, final_);
                                        Toast.makeText(Image_Upload.this, "Lungs Segmented Successfully !", Toast.LENGTH_SHORT).show();
                                        imageView.setImageBitmap(final_);
                                        progressDialog.setTitle("Generating Inference");
                                        progressDialog.show();
                                    }
                                }
                            },1700);


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

                             topy = Collections.min(y);
                             topx = Collections.min(x);
                             bottomy = Collections.max(y);
                             bottomx = Collections.max(x);

                            Mat sub = final_one.submat(topx,bottomx,topy,bottomy);
                            final Bitmap final_cropped = Bitmap.createBitmap(sub.cols(), sub.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(sub, final_cropped);
                            Log.e(TAG,"Function came last");

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    reached=true;
                                    imageView.setImageBitmap(final_cropped);
                                    getInferenceNeuralNet(bmp);
                                  //  progressDialog.cancel();
                                }
                            },1500);
                        }
                    });
t.start();
                }
                else{
                    Toast.makeText(Image_Upload.this, "Lungs segmentation Unsuccessful !!", Toast.LENGTH_SHORT).show();
                    getInferenceNeuralNet(bmp);
                }


    }

    private void getInferenceNeuralNet(Bitmap bitmap){

        predict.setVisibility(View.GONE);
        load.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        final Handler j = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()){
                    progressDialog.cancel();
                }

                int a = progressBar.getProgress();
                if(a<((int)Double.parseDouble(covid))){
                    progressBar.setProgress(a+1);
                    j.postDelayed(this,5);
                }
                else{

                    Toast.makeText(Image_Upload.this, "Inference Generated !", Toast.LENGTH_SHORT).show();
                    textView.setText("Results: Covid = "+covid+"\n         Non-Covid = "+noncovid);
                    if(Double.parseDouble(covid)>=50.0){
                        saliencymap_gen.setVisibility(View.VISIBLE);
                    }
                    else{
                        Toast.makeText(Image_Upload.this, "No-Covid Prediction!\nCan't Generate Saliency Map", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp_test = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                Bitmap finaul = Bitmap.createScaledBitmap(bmp_test, 512, 512, true);

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
                interpreter.close();
                input.clear();
                finaul.recycle();
                bmp_test.recycle();

                modelOutput.rewind();
                FloatBuffer probabilities = modelOutput.asFloatBuffer();

                modelOutput.clear();
                probabilities.rewind();
                covid = (probabilities.get(0)*100)+"";
                noncovid = (probabilities.get(1)*100)+"";
                j.post(r);
            }
        });
        temp.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode==999 && resultCode==Activity.RESULT_OK){
           try {
                uri = data.getData();
               imageView.setImageURI(uri);
           }
           catch (Exception e){
               Toast.makeText(this, "Problem While loading image !", Toast.LENGTH_SHORT).show();
           }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
