package com.example.messenger_project;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageBtn;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayMessageText;

    private String currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        InitializeFields();
    }

    private void InitializeFields()
    {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageBtn = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayMessageText = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);
    }
}