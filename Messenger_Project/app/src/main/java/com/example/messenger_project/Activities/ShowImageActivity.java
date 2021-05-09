package com.example.messenger_project.Activities;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.R;
import com.ortiz.touchview.TouchImageView;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class ShowImageActivity extends AppCompatActivity {

    private BitmapDrawable drawable;
    private Bitmap bitmap;
    private TouchImageView imageView;
    private ConstraintLayout showImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_show_image);
        Toolbar toolbar = findViewById(R.id.showImage_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ColorDrawable colorDrawable
                = new ColorDrawable(getResources().getColor(R.color.black));
        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        Intent i = getIntent();
        String image = i.getStringExtra("image");

        imageView = findViewById(R.id.imageView);
        showImageLayout = findViewById(R.id.show_image_layout);
        Picasso.get().load(image).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_image_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.save_image_button)
        {
            Drawable drawable = imageView.getDrawable();
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            try {
                CheckPermission();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 112)
        {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    try {
                        saveToInternalStorage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private void saveToInternalStorage() throws IOException {
            File filePath = Environment.getExternalStorageDirectory();
            File dir = new File(filePath.getAbsolutePath() + "/DCIM/" + "/Discussion/");
            dir.mkdir();
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(file) {
            };
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Toasty.success(getApplication(), "Image Save to " + dir, Toasty.LENGTH_SHORT).show();
            outputStream.flush();
            outputStream.close();
    }

    private void CheckPermission() throws IOException {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            saveToInternalStorage();
        } else {
            ActivityCompat.requestPermissions(
                    ShowImageActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112);
        }

    }
}