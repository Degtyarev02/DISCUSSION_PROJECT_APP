package com.example.messenger_project.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.messenger_project.R;
import com.squareup.picasso.Picasso;

public class ShowImageActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Intent i = getIntent();
        String image = i.getStringExtra("image");

        ImageView imageView = findViewById(R.id.imageView);
        Picasso.get().load(image).into(imageView);
    }
}