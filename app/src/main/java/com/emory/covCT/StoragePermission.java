package com.emory.covCT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StoragePermission extends AppCompatActivity {

    Button storage_permit;
    TextView text_skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_permission);

        //Checks if the permission is already given, then skips to Select_toon_style
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) && (
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )== PackageManager.PERMISSION_GRANTED)){
            startActivity(new Intent(StoragePermission.this,HomeOptions.class));
            finish();
        }

        initView();
    }

    private void initView(){
        storage_permit = findViewById(R.id.storage);
        text_skip = findViewById(R.id.skip_text);

        storage_permit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(StoragePermission.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},102);
            }
        });

        text_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StoragePermission.this,HomeOptions.class));
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==102 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permissions granted !", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StoragePermission.this,HomeOptions.class));
            finish();
        }
        else{
            Toast.makeText(this, "Permissions Denied !", Toast.LENGTH_SHORT).show();
        }
    }
}
