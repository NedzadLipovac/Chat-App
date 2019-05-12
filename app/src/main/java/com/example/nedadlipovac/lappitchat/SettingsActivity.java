package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Addroid Layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;
    public static final Integer GALERY_PICKER = 1;
    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mUserDataBase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorageReference;
    //Progress
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mUsersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

//firebse code
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mImageStorageReference = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDataBase.keepSynced(true);
        //set get layout value
        mName = (TextView) findViewById(R.id.settingsName);
        mStatus = (TextView) findViewById(R.id.settingsStatus);
        mStatusBtn = (Button) findViewById(R.id.settingsStatusBtn);
        mImageBtn = (Button) findViewById(R.id.settingsImageBtn);

        mDisplayImage = (CircleImageView) findViewById(R.id.profile_image);
        ///pokusaj


        mUserDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                mName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.ic_launcher_profile1_round)
                            .into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(SettingsActivity.this).load(image)
                                            .placeholder(R.mipmap.ic_launcher_profile1_round)
                                            .into(mDisplayImage);
                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent, "SELECT IMAGE"), GALERY_PICKER);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_PICKER && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
//progress dialog
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we wait and process the image");
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog.show();


                //upload uri..
                Uri resultUri = result.getUri();
                final File thum_filePath = new File(resultUri.getPath());
                final String current_uid = mCurrentUser.getUid();
                byte[] thumb_byte = new byte[0];
                //just have to add this code for image compressor -->https://github.com/zetbaitsu/Compressor
                try {
                    Bitmap compressedImageBitmap = new Compressor(this)
                            .setMaxHeight(550)
                            .setMaxWidth(550)
                            .setQuality(70)
                            .compressToBitmap(thum_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final byte[] thumb_byte1 = thumb_byte;
                final StorageReference filepath = mImageStorageReference.child("profile_images").child(current_uid + ".jpg");

                //**start uploading main image**
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {


                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Picture uploaded", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
//get download thumb pic link ... and set
                            //need StorageReference to get download url bc new firebase version
                            final StorageReference filePath = mImageStorageReference.child("profile_images").child(current_uid + ".jpg");
                            final StorageReference ThumbfilePath = mImageStorageReference.child("profile_images").child("thumbs").child(current_uid + ".jpg");

                            //***uplaod thumb image some bad things happens***
                            UploadTask uploadTask = (UploadTask) ThumbfilePath.putBytes(thumb_byte1);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {
                                        ThumbfilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                mUserDataBase.child("thumb_image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mProgressDialog.dismiss();

                                                        } else {
                                                            Toast.makeText(SettingsActivity.this, "In* ERROR in uploading thumbs...", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        mProgressDialog.dismiss();
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Out ERROR in uploading thumbs...", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
//upload task for thumbs finished... need to fix some bugs
                            //set  real image after getting uri
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    mUserDataBase.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mProgressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });

                        } else {
                            Toast.makeText(SettingsActivity.this, "ERROR in uploading", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser!=null){
            mUsersRef.child("online").setValue("true");
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrentUser!=null){
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }
}
