package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.print.PrinterId;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText mDisplayName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    //progressDialog
    ProgressDialog mRegProgress;
    private DatabaseReference mDataBase;
    private DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        mAuth = FirebaseAuth.getInstance();
        mRegProgress = new ProgressDialog(this);

        mDisplayName = (EditText) findViewById(R.id.regDisplayName);
        mEmail = findViewById(R.id.regEmail);
        mPassword = findViewById(R.id.regPassword);
        mCreateBtn = (Button) findViewById(R.id.regCreateBtn);
        //ToolBar set
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.registerToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (!TextUtils.isEmpty(mDisplayName.getText().toString()) || !TextUtils.isEmpty(mEmail.getText().toString()) || !TextUtils.isEmpty(mPassword.getText().toString())) {
                    mRegProgress.show();
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    registerUser(display_name, email, password);

                }

            }


        });
    }

    private void registerUser(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mDataBase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String,String> userMap=new HashMap<>();
                            userMap.put("name",display_name);
                            userMap.put("status","Hi there, Iam using Lapit Chat App");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token",deviceToken);
                            mDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        mRegProgress.dismiss();
                                    }
                                }
                            });
                        } else {
                            // Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mRegProgress.hide();
                        }
                    }
                });

    }


}
