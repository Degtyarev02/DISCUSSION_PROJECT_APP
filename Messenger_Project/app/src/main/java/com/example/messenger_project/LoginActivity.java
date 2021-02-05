package com.example.messenger_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private Button LoginBtn, PhoneLoginBtn;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccount, ForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeField();

        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();
            }
        });
    }

    private void InitializeField() //Функция инициализирует все поля в активити для входа
    {
        LoginBtn = findViewById(R.id.login_button);
        PhoneLoginBtn = findViewById(R.id.phone_login_button);

        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);

        NeedNewAccount = findViewById(R.id.need_new_account);
        ForgetPassword = findViewById(R.id.forget_password_link);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser != null)
        {
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity()
    {
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class );
        startActivity(loginIntent);
    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class );
        startActivity(registerIntent);
    }
}
