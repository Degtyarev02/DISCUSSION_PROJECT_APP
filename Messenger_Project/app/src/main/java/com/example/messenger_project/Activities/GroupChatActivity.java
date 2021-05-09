package com.example.messenger_project.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.roger.catloadinglibrary.CatLoadingView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView SendMessageBtn, sendFileMessageButton;
    private EditText userMessageInput;
    private RecyclerView mScrollView;
    private TextView displayMessageText;
    private MessageAdapter messageAdapter;
    private LinearLayoutManager linearLayoutManager;

    private CatLoadingView catProgressDialog;

    private StorageTask uploadTask;
    private String selectedFileType = "", myUrl = "";
    private Uri fileURI;

    private final List<Messages> messagesList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        /*getWindow().setBackgroundDrawableResource(R.drawable.it_background);*/

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().get("groupName").toString();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        catProgressDialog = new CatLoadingView();

        InitializeFields();
        GetUserInfo();
        DisplayMessage();
        RetrieveMessage();

        SendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userMessageInput.getText().toString();
                if (!message.isEmpty()) {
                    SaveMessageInfoToDatabase();
                    userMessageInput.setText("");
                }
            }
        });

        sendFileMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
                currentTime = currentTimeFormat.format(calForTime.getTime());

                FlatDialog flatDialog = new FlatDialog(GroupChatActivity.this);
                flatDialog
                        .setTitle("Select file")
                        .setFirstButtonText("IMAGE")
                        .setSecondButtonText("PDF")
                        .setThirdButtonText("DOC")

                        .setBackgroundColor(getResources().getColor(R.color.white))

                        .setFirstButtonColor(getResources().getColor(R.color.Gray))
                        .setSecondButtonColor(getResources().getColor(R.color.DarkBlue))
                        .setThirdButtonColor(getResources().getColor(R.color.DarkRed))

                        .setFirstButtonTextColor(getResources().getColor(R.color.blackyGray))
                        .setSecondButtonTextColor(getResources().getColor(R.color.whity_gray))
                        .setThirdButtonTextColor(getResources().getColor(R.color.whity_gray))

                        .setTitleColor(getResources().getColor(R.color.DarkBlue))
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        catProgressDialog.show(getSupportFragmentManager(), "");
        String messageKey = GroupNameRef.push().getKey();
        GroupMessageKeyRef = GroupNameRef.child(messageKey);

        if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileURI = data.getData();


            if (!selectedFileType.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                StorageReference filePath = storageReference.child(messageKey + "." + selectedFileType);

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
                                messageTextBody.put("from", currentUserID);
                                messageTextBody.put("name", currentUserName);
                                messageTextBody.put("messageID", messageKey);
                                messageTextBody.put("time", currentTime);

                                GroupMessageKeyRef.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        catProgressDialog.dismiss();
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                catProgressDialog.dismiss();
                                Toasty.error(GroupChatActivity.this, e.getMessage(), Toasty.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }


            else if (selectedFileType.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");


                StorageReference filePath = storageReference.child(messageKey + "." + "jpg");
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

                            catProgressDialog.dismiss();
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map<String, Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("filename", fileURI.getLastPathSegment());
                            messageTextBody.put("type", selectedFileType);
                            messageTextBody.put("from", currentUserID);
                            messageTextBody.put("name", currentUserName);
                            messageTextBody.put("messageID", messageKey);
                            messageTextBody.put("time", currentTime);

                            GroupMessageKeyRef.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    catProgressDialog.setBackgroundColor(getResources().getColor(R.color.DarkBlue));
                                }
                            });;
                        }
                    }
                });
            } else {
                Toasty.error(this, "Nothing selected", Toasty.LENGTH_SHORT).show();
            }
        }
    }







    private void RetrieveMessage() {
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {

                    Messages messages = snapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                    mScrollView.smoothScrollToPosition(mScrollView.getAdapter().getItemCount());
                }
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


    private void InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageBtn = findViewById(R.id.send_message_button);
        sendFileMessageButton = findViewById(R.id.send_groupChat_file_ImageButton);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
    }

    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if (!TextUtils.isEmpty(message)) {
            /*Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());
*/
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);
            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("from", currentUserID);
            messageInfoMap.put("message", message);
            messageInfoMap.put("type", "text");
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }

    }


    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
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

    private void DisplayMessage() {
        messageAdapter = new MessageAdapter(getApplicationContext(), messagesList, currentGroupName);
        linearLayoutManager = new LinearLayoutManager(this);
        mScrollView.setLayoutManager(linearLayoutManager);
        mScrollView.setAdapter(messageAdapter);
    }
}