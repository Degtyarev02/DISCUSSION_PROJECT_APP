package com.example.messenger_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Logo_activity extends AppCompatActivity {

    private Animation LogoAnim, ButtonAnim, textAnim;
    private Button bAnim;
    private ImageView lAnim;
    private TextView mainText;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_activity);
        anim();
        startMainActivity();
    }


    public void startMainActivity()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(Logo_activity.this, MainActivity.class);
                startActivity(i);
                onDestroy();
            }
        }).start();


    }

    public void anim()
    {
        lAnim = findViewById(R.id.imageView2);
        mainText = findViewById(R.id.textView);

        LogoAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.first_anim);
        textAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.first_anim);

        mainText.startAnimation(textAnim);
        lAnim.startAnimation(LogoAnim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}