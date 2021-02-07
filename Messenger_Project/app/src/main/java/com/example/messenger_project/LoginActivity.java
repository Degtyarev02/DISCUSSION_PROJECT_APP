package com.example.messenger_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private Button LoginBtn, PhoneLoginBtn;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccount, ForgetPassword;
    private CheckBox ShowPassword;

    private FirebaseAuth MyAuth;
    private ProgressDialog LoggedBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MyAuth = FirebaseAuth.getInstance();

        InitializeField();

        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();
            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        ShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) UserPassword.setTransformationMethod(new HideReturnsTransformationMethod());
                else UserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    private void AllowUserToLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ) Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show();
        else
        {

            //установка введенного логина и пароля в учетную запись файрбэйз
            MyAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(LoginActivity.this, "Logged in Successful!", Toast.LENGTH_LONG);
                                SendUserToMainActivity();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error" + message, Toast.LENGTH_LONG);
                            }
                        }
                    });
        }
    }




    private void InitializeField() //Функция инициализирует все поля в активити для входа
    {
        LoginBtn = findViewById(R.id.login_button);
        PhoneLoginBtn = findViewById(R.id.phone_login_button);

        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        ShowPassword = findViewById(R.id.ShowLoginPassword);

        NeedNewAccount = findViewById(R.id.need_new_account);
        ForgetPassword = findViewById(R.id.forget_password_link);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class );
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class );
        startActivity(registerIntent);
    }
}
