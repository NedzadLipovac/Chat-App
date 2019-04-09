package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private EditText mStatus;
    private Button mSaveBtn;
    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrent_User;
    //Progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //Firebase
        mCurrent_User = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrent_User.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        //layout
        mToolBar = (Toolbar) findViewById(R.id.statusAppBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeButtonEnabled(true);
        mStatus = (EditText) findViewById(R.id.statusInput);
        mSaveBtn = (Button) findViewById(R.id.statusSave);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus.setText(status_value);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saveing Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String status = mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "There was some erros in saveing changes ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }
}
