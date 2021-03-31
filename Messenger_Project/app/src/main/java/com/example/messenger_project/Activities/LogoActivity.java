package com.example.messenger_project.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.messenger_project.R;

public class LogoActivity extends Activity {


    private Animation LogoAnim, ButtonAnim, textAnim;
    private Button bAnim;
    private LottieAnimationView lAnim;
    private TextView mainText;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        anim();
        startMainActivity();
    }


    public void startMainActivity()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(i);
                onDestroy();
            }
        }).start();


    }

    public void anim()
    {
        lAnim = findViewById(R.id.imageView2);
        mainText = findViewById(R.id.textView);

        /*LogoAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_anim);*/
        lAnim.animate().translationY(-1400).setDuration(1000).setStartDelay(2500);/*
        textAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.first_anim);
        mainText.startAnimation(textAnim);
        lAnim.startAnimation(LogoAnim);*/
        mainText.animate().translationY(1600).setDuration(1000).setStartDelay(2500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}