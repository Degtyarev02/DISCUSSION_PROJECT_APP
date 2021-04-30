package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.Adapters.MessageAdapter;
import com.example.messenger_project.Messages;
import com.example.messenger_project.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage;

    private TextView userName, userLastSeen, noMessageView;
    private CircleImageView userProfImage;
    private ImageView sendMessageBtn, sendFileMessageButton;
    private EditText messageInputText;
    private ImageView greenDotOnlineStatus;
    private String selectedFileType = "", myUrl = "";
    private Uri fileURI;

    private FirebaseAuth mAuth;
    private DatabaseReference Chats, RootRef, UserRef;
    private String currentUserId, currentUserName;
    private String saveCurrentTime, saveCurrentDate;
    private StorageTask uploadTask;

    private Toolbar chatToolBar;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setBackgroundDrawableResource(R.drawable.background_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_Id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeControllers();
        GetUserInfo();
        displayLastSeen();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.man_user).into(userProfImage);


        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInputText.getText().toString();
                if (!message.isEmpty()) {
                    SendMessage();
                }
            }
        });

        sendFileMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatDialog flatDialog = new FlatDialog(ChatActivity.this);
                flatDialog
                        .setTitle("Select file")
                        .setFirstButtonText("IMAGE")
                        .setSecondButtonText("PDF")
                        .setThirdButtonText("DOC")

                        .setBackgroundColor(getResources().getColor(R.color.white))

                        .setFirstButtonColor(getResources().getColor(R.color.ReallyGray))
                        .setSecondButtonColor(getResources().getColor(R.color.purple_700))
                        .setThirdButtonColor(getResources().getColor(R.color.Gray))

                        .setFirstButtonTextColor(getResources().getColor(R.color.blackyGray))
                        .setSecondButtonTextColor(getResources().getColor(R.color.whity_gray))
                        .setThirdButtonTextColor(getResources().getColor(R.color.whity_gray))

                        .setTitleColor(getResources().getColor(R.color.purple_700))
                        .withFirstButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedFileType = "image";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent, "Select image"), 5);
                                flatDialog.dismiss();
                            }
                        })
                        .withSecondButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedFileType = "pdf";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/pdf");
                                startActivityForResult(intent.createChooser(intent, "Select PDF file"), 5);
                                flatDialog.dismiss();
                            }
                        })
                        .withThirdButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedFileType = "docx";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                                startActivityForResult(Intent.createChooser(intent, "Select MS Word File"), 5);
                                flatDialog.dismiss();
                            }
                        })
                        .show();

            }
        });

        RetrieveMessageList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileURI = data.getData();

            if (!selectedFileType.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(currentUserId).child(messageReceiverID).push();

                String messagePushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushId + "." + selectedFileType);

                filePath.putFile(fileURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                Map<String, Object> messageTextBody = new HashMap<>();
                                messageTextBody.put("message", downloadUrl);
                                messageTextBody.put("filename", fileURI.getLastPathSegment());
                                messageTextBody.put("type", selectedFileType);
                                messageTextBody.put("from", currentUserId);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("name", currentUserName);
                                messageTextBody.put("messageID", messagePushId);
                                messageTextBody.put("time", saveCurrentTime);

                                userMessageKeyRef.updateChildren(messageTextBody);

                                DatabaseReference receiver_to_sender_message = RootRef.child("Messages").child(messageReceiverID)
                                        .child(currentUserId).child(messagePushId);
                                receiver_to_sender_message.updateChildren(messageTextBody);
                                /*loadingBar.dismiss();*/

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                /*loadingBar.dismiss();*/
                                Toasty.error(ChatActivity.this, e.getMessage(), Toasty.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        /*loadingBar.setMessage((int) p + " % Uploading...");*/
                    }
                });
            }

            else if (selectedFileType.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(currentUserId).child(messageReceiverID).push();

                String messagePushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileURI);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) throw task.getException();
                        return filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map<String, Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("filename", fileURI.getLastPathSegment());
                            messageTextBody.put("type", selectedFileType);
                            messageTextBody.put("from", currentUserId);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("name", currentUserName);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);

                            userMessageKeyRef.updateChildren(messageTextBody);

                            DatabaseReference receiver_to_sender_message = RootRef.child("Messages").child(messageReceiverID)
                                    .child(currentUserId).child(messagePushId);
                            receiver_to_sender_message.updateChildren(messageTextBody);
                        }
                    }
                });
            } else {
                Toasty.error(this, "Nothing selected", Toasty.LENGTH_SHORT).show();
            }
        }
    }

    private void RetrieveMessageList() {

        RootRef.child("Messages").child(currentUserId).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    noMessageView.setVisibility(View.VISIBLE);
                    userMessagesList.setVisibility(View.INVISIBLE);

                } else {
                    noMessageView.setVisibility(View.INVISIBLE);
                    userMessagesList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        RootRef.child("Messages").child(currentUserId).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        displayLastSeen();
    }

    private void InitializeControllers() {
        chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userProfImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_prof_name);
        userLastSeen = findViewById(R.id.custom_prof_online_status);
        greenDotOnlineStatus = findViewById(R.id.green_dot_chatbar_online_status);

        sendFileMessageButton = findViewById(R.id.send_file_ImageButton);
        sendMessageBtn = findViewById(R.id.send_chat_message_button);
        messageInputText = findViewById(R.id.input_chat_message);
        noMessageView = findViewById(R.id.no_messages_view);

        messageAdapter = new MessageAdapter(getApplicationContext(), messagesList);
        userMessagesList = findViewById(R.id.private_messenger_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }


    private void GetUserInfo() {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendMessage() {
        String messagetext = messageInputText.getText().toString();
        if (!TextUtils.isEmpty(messagetext)) {


            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(currentUserId).child(messageReceiverID).push();
            String messagePushId = userMessageKeyRef.getKey();

            Map<String, Object> messageTextBody = new HashMap<>();
            messageTextBody.put("message", messagetext);
            messageTextBody.put("filename", "text Message");
            messageTextBody.put("type", "text");
            messageTextBody.put("from", currentUserId);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("name", currentUserName);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", saveCurrentTime);

            userMessageKeyRef.updateChildren(messageTextBody);
            DatabaseReference receiver_to_sender_message = RootRef.child("Messages").child(messageReceiverID).child(currentUserId).child(messagePushId);
            receiver_to_sender_message.child(messagePushId);
            receiver_to_sender_message.updateChildren(messageTextBody);
            messageInputText.setText("");
        }
    }

    private void displayLastSeen() {

        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("State", "online");

        RootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineStatus);

        UserRef.child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("State")) {
                    String state = snapshot.child("userState").child("State").getValue().toString();
                    String date = snapshot.child("userState").child("Date").getValue().toString();
                    String time = snapshot.child("userState").child("Time").getValue().toString();

                    if (state.equals("online")) {
                        userLastSeen.setText("Online");
                        greenDotOnlineStatus.setVisibility(View.VISIBLE);
                    } else {
                        String offline = "Last seen: " + date + " " + time;
                        userLastSeen.setText(offline);
                        greenDotOnlineStatus.setVisibility(View.INVISIBLE);
                    }
                } else {
                    userLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}