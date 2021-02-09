package com.example.messenger_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText UserName, UserStatus;
    private Button UpdateInfo;
    private ImageView UserIcon;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        Initialize();
        RetrieveUserInfo();

        UpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
    }


    private void Initialize()
    {
        UserIcon = findViewById(R.id.profile_image);
        UserName = findViewById(R.id.set_username);
        UserStatus = findViewById(R.id.set_user_status);
        UpdateInfo = findViewById(R.id.update_button);
    }

    private void UpdateSettings()
    {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();

        HashMap<String, String> ProfileMap = new HashMap<>();
            ProfileMap.put("uid", currentUserId);
            ProfileMap.put("name", setUserName);
            ProfileMap.put("status", setStatus);

        if(!TextUtils.isEmpty(setUserName))
        {
            RootRef.child("Users").child(currentUserId).setValue(ProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Update is Successfully", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                        }
                    });
        }
        else Toast.makeText(SettingsActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if((snapshot.exists()) && (snapshot.hasChild("name") && snapshot.hasChild("image")))
                        {
                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();
                            String retrieveProfileImage = snapshot.child("image").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveUserStatus);

                            UserName.setEnabled(false);
                        }
                        else if((snapshot.exists()) && (snapshot.hasChild("name")))
                        {
                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveUserStatus);

                            UserName.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class );
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}