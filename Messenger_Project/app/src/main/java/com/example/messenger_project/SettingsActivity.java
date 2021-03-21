package com.example.messenger_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText UserName, UserStatus;
    private Button UpdateInfo;
    private CircleImageView UserIcon;

    private String photoURL;

    private FirebaseAuth mAuth;
    private String currentUserId, retrieveEmail, retrievePassword;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference userProfImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userProfImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        retrieveEmail = snapshot.child("email").getValue().toString();
                        retrievePassword = snapshot.child("password").getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });

        Initialize();
        RetrieveUserInfo();

        UpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        UserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            final String downloaedUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    final String downloaedUrl = uri.toString();

                                    RootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloaedUrl);
                                }
                            });
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings()
    {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();

        HashMap<String, String> ProfileMap = new HashMap<>();
            ProfileMap.put("uid", currentUserId);
            ProfileMap.put("name", setUserName);
            ProfileMap.put("status", setStatus);
            ProfileMap.put("image", photoURL);
            ProfileMap.put("email", retrieveEmail);
            ProfileMap.put("password", retrievePassword);

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
                            photoURL = retrieveProfileImage;

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveUserStatus);
                            Picasso.get().load(retrieveProfileImage).into(UserIcon);

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