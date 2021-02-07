package com.example.messenger_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText UserName, UserStatus;
    private Button UpdateInfo;
    private CircleImageView UserIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Initialize();
    }

    private void Initialize()
    {
        UserIcon = findViewById(R.id.profile_image);
        UserName = findViewById(R.id.set_username);
        UserStatus = findViewById(R.id.set_user_status);
        UpdateInfo = findViewById(R.id.update_button);
    }
}