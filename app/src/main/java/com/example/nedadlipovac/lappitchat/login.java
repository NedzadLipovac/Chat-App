package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.transition.Fade.IN;

public class login extends AppCompatActivity {
    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mLoginBtn;
    private Toolbar mToolBar;
    private ProgressDialog mLoginProgres;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private Button mTestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginProgres = new ProgressDialog(this);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolBar = (Toolbar) findViewById(R.id.LoginToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginEmail = (EditText) findViewById(R.id.logEmail);
        mLoginPassword = (EditText) findViewById(R.id.logPassword);
        mLoginBtn = (Button) findViewById(R.id.logLoginBtn);
        mAuth = FirebaseAuth.getInstance();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getText().toString();
                String password = mLoginPassword.getText().toString();
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mLoginProgres.setTitle("Logging in");
                    mLoginProgres.setMessage("Please wait while we check your credentials");
                    mLoginProgres.setCanceledOnTouchOutside(false);
                    mLoginProgres.show();
                    loginUser(email, password);
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoginProgres.dismiss();

                            String current_user_uid=mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUsersDatabase.child(current_user_uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainIntent = new Intent(login.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    finish();
                                }
                            });

                        } else {
                            mLoginProgres.hide();
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
