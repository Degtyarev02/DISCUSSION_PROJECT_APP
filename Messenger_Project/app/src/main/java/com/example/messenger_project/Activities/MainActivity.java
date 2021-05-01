package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.Adapters.TabsAccessorAdapter;
import com.example.messenger_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccAdapter;
    private String currentUserID, saveCurrentTime, saveCurrentDate;;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        try {
            currentUserID = currentUser.getUid();
        } catch (Exception e) {
            System.err.println(e);
        }

        mToolBar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Discussion");

        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsAccAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser == null)
        {
           SendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyExistenceUser();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateUserStatus("offline");
                }
            }).start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void VerifyExistenceUser()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {if(!(snapshot.child("name").exists())) SendUserToSettingsActivity();}

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }


    // Creating option menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case(R.id.main_find_friends):
                SendUserToFindFriends();
                break;
            case(R.id.main_settings):
                SendUserToSettingsActivity();
                break;
            case(R.id.main_sign_out):
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
            case(R.id.main_create_group):
                RequestNewGroup();
                break;
        }
        return true;
    }


    private void RequestNewGroup()
    {
        FlatDialog flatDialog = new FlatDialog(MainActivity.this);
        flatDialog
                .setTitle("Create new Group")
                .setFirstTextFieldHint("Group name")
                .setFirstButtonText("Create")
                .setSecondButtonText("Cancel")
                .setBackgroundColor(getResources().getColor(R.color.white))
                .setFirstButtonColor(getResources().getColor(R.color.purple_700))
                .setFirstButtonTextColor(getResources().getColor(R.color.whity_gray))
                .setSecondButtonColor(getResources().getColor(R.color.Gray))
                .setSecondButtonTextColor(getResources().getColor(R.color.whity_gray))
                .setTitleColor(getResources().getColor(R.color.purple_700))
                .setFirstTextFieldBorderColor(getResources().getColor(R.color.Gray))
                .setFirstTextFieldTextColor(getResources().getColor(R.color.blackyGray))
                .setFirstTextFieldHintColor(getResources().getColor(R.color.blackyGray))
                .withFirstButtonListner(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String groupName = flatDialog.getFirstTextField();
                        if(!TextUtils.isEmpty(groupName))
                        {
                            CreateNewGroup(groupName);
                            flatDialog.dismiss();
                        }
                    }
                })
                .withSecondButtonListner(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        flatDialog.dismiss();
                    }
                })
                .show();
    }

    private void CreateNewGroup(String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + " is create", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class );
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriends()
    {
        Intent FindFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class );
        startActivity(FindFriendsIntent);
    }

    private void updateUserStatus(String state)
    {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStatus = new HashMap<>();

        onlineStatus.put("Time", saveCurrentTime);
        onlineStatus.put("Date", saveCurrentDate);
        onlineStatus.put("State", state);

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStatus);
    }
}