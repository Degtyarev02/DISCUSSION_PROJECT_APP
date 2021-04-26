package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.R;
import com.ortiz.touchview.TouchImageView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Intent i = getIntent();
        String image = i.getStringExtra("image");

        TouchImageView imageView = findViewById(R.id.imageView);
        Picasso.get().load(image).into(imageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FlatDialog flatDialog = new FlatDialog(ShowImageActivity.this);
                flatDialog
                        .setTitle("Save Image?")
                        .setTitleColor(getResources().getColor(R.color.Gray))
                        .setFirstButtonText("Save")
                        .setSecondButtonText("Cancel")
                        .setBackgroundColor(getResources().getColor(R.color.white))
                        .setFirstButtonColor(getResources().getColor(R.color.ReallyGray))
                        .setSecondButtonColor(getResources().getColor(R.color.Gray))
                        .setFirstButtonTextColor(getResources().getColor(R.color.blackyGray))
                        .setSecondButtonTextColor(getResources().getColor(R.color.whity_gray))
                        .withFirstButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                {
                                    Drawable drawable = imageView.getDrawable();
                                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                                    try {
                                        CheckPermission();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    flatDialog.dismiss();
                                }
                            }
                        })
                        .withSecondButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                flatDialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
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
            File dir = new File(filePath.getAbsolutePath());
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