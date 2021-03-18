package com.example.messenger_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;
    private CircleImageView profileImage;
    private TextView userName;
    private TextView userStatus;
    private Button sendMessage;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();

        profileImage = findViewById(R.id.visit_profile_image);
        userName = findViewById(R.id.visit_username);
        userStatus = findViewById(R.id.visit_status);
        sendMessage = findViewById(R.id.request_message_btn);

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.exists()) && (snapshot.hasChild("image")))
                {
                    String userImage = snapshot.child("image").getValue().toString();
                    String Set_userName = snapshot.child("name").getValue().toString();
                    String Set_userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.man).into(profileImage);
                    userName.setText(Set_userName);
                    userStatus.setText(Set_userStatus);
                }
                else
                {
                    String Set_userName = snapshot.child("name").getValue().toString();
                    String Set_userStatus = snapshot.child("status").getValue().toString();

                    userName.setText(Set_userName);
                    userStatus.setText(Set_userStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}