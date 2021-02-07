package com.example.messenger_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button RegisterBtn;
    private EditText NewEmail, NewPassword;
    private TextView HaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;
    private ProgressDialog progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootReference = FirebaseDatabase.getInstance().getReference();


        RegisterInitialize();

        HaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 SendUserToLoginActivity();
            }
        });
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email = NewEmail.getText().toString();
        String password = NewPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) )
        {
            Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressBar.setTitle("Creating new account");
            progressBar.setMessage("Please Wait");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();


            //установка введенного логина и пароля в учетную запись файрбэйз
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                String userId = mAuth.getCurrentUser().getUid();
                                RootReference.child("Users").child(userId).setValue(" ");

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Successful!", Toast.LENGTH_LONG);
                                progressBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error" + message, Toast.LENGTH_LONG);
                                progressBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void RegisterInitialize()
    {
        RegisterBtn = findViewById(R.id.register_button);
        NewEmail = findViewById(R.id.register_email);
        NewPassword = findViewById(R.id.register_password);
        HaveAccount = findViewById(R.id.already_have_account);
        progressBar = new ProgressDialog(this);

    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class );
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class );
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}