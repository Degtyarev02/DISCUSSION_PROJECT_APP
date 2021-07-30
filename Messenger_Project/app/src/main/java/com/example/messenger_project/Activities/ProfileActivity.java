package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.messenger_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, senderUserID;
    private CircleImageView profileImage;
    private TextView userName;
    private TextView userStatus;
    private Button sendMessage, DeclineReqBtn;
    private TextView goToInstagramProfileButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        InitializeContent();
        RetrieveUserInfo();

        goToInstagramProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("instagram")) {
                            try {
                                String instagramRetrievingUrl = snapshot.child("instagram").getValue().toString();
                                Uri uri = Uri.parse(instagramRetrievingUrl);
                                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                                likeIng.setPackage("com.instagram.android");
                                startActivity(likeIng);
                            } catch (ActivityNotFoundException e) {
                                Toasty.error(ProfileActivity.this, "Error", Toasty.LENGTH_SHORT).show();
                            }
                        } else {
                            Toasty.warning(ProfileActivity.this, "User hasn't added an account", Toasty.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }


    private void RetrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {
                    String userImage = snapshot.child("image").getValue().toString();
                    String Set_userName = snapshot.child("name").getValue().toString();
                    String Set_userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.man).into(profileImage);

                    String[] shortName = Set_userName.split(" ");
                    if (shortName.length == 1) {
                        userName.setText(Set_userName);
                    } else {
                        userName.setText(shortName[0] + " " + shortName[1].substring(0, 1) + ".");
                    }
                    userStatus.setText(Set_userStatus);

                    ManageChatRequest();
                } else {
                    String Set_userName = snapshot.child("name").getValue().toString();
                    String Set_userStatus = snapshot.child("status").getValue().toString();

                    userName.setText(Set_userName);
                    userStatus.setText(Set_userStatus);
                    ManageChatRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {
        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)) {
                            String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                currentState = "request_send";
                                sendMessage.setText(R.string.CancelChatRequest);
                            } else if (request_type.equals("received")) {
                                currentState = "request_received";
                                sendMessage.setText(R.string.AcceptChatRequest);
                                DeclineReqBtn.setVisibility(View.VISIBLE);
                                DeclineReqBtn.setEnabled(true);
                                DeclineReqBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            contactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserID)) {
                                                currentState = "friends";
                                                sendMessage.setText("Remove contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)) {
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage.setEnabled(false);
                    if (currentState.equals("new")) {
                        SendChatRequest();
                    }
                    if (currentState.equals("request_send")) {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends")) {
                        RemoveContact();
                    }
                }
            });
        } else {
            sendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {
        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if (task.isSuccessful()) {
                                                                       sendMessage.setEnabled(true);
                                                                       currentState = "new";
                                                                       sendMessage.setText("Send Message");
                                                                       DeclineReqBtn.setVisibility(View.GONE);
                                                                       DeclineReqBtn.setEnabled(false);
                                                                   }
                                                               }
                                                           }
                                    );
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        contactsRef.child(senderUserID).
                child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).
                                    child(senderUserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessage.setEnabled(true);
                                                                    currentState = "friends";
                                                                    sendMessage.setText("Remove contact");
                                                                    DeclineReqBtn.setVisibility(View.GONE);
                                                                    DeclineReqBtn.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()) {
                                                   chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          if (task.isSuccessful()) {
                                                                                              sendMessage.setEnabled(true);
                                                                                              currentState = "new";
                                                                                              sendMessage.setText("Send Message");
                                                                                              DeclineReqBtn.setVisibility(View.GONE);
                                                                                              DeclineReqBtn.setEnabled(false);
                                                                                          }
                                                                                      }
                                                                                  }
                                                           );
                                               }

                                           }
                                       }
                );
    }

    private void SendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessage.setEnabled(true);
                                                currentState = "request_send";
                                                sendMessage.setText("Cancel Chat request");
                                                DeclineReqBtn.setVisibility(View.GONE);
                                                DeclineReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeContent() {
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        profileImage = findViewById(R.id.visit_profile_image);
        userName = findViewById(R.id.visit_username);
        userStatus = findViewById(R.id.visit_status);
        sendMessage = findViewById(R.id.request_message_btn);
        DeclineReqBtn = findViewById(R.id.decline_request);
        goToInstagramProfileButton = findViewById(R.id.go_to_instagram);
        currentState = "new";
    }
}