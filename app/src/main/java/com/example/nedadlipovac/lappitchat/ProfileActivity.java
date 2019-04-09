package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mProfileDeclineBtn;
    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private String mCurrent_state;
    private DatabaseReference mFiriendReqDatabase, mNotificationsDataBase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootref;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();

        mUsersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        final String user_id = getIntent().getStringExtra("user_id");

        mProfileImage=(CircleImageView)findViewById(R.id.mProfileImage);
        mProfileName = (TextView) findViewById(R.id.profileName);
        mProfileStatus = (TextView) findViewById(R.id.profileStatus);
        //mProfileFriendsCount = (TextView) findViewById(R.id.profileFriendsCount);
        mProfileSendReqBtn = (Button) findViewById(R.id.profileSendReqBtn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profileDeclineReqBtn);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFiriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationsDataBase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootref = FirebaseDatabase.getInstance().getReference();

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(true);

        mCurrent_state = "not_friends";
        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image)
                        .placeholder(R.mipmap.ic_launcher_profile56)
                        .into(mProfileImage);

                //=================================Friends List/request feature=====================
                mFiriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {

                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child((mCurrent_user.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "Friends";
                                        mProfileSendReqBtn.setText("UnFriend this Person");
                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                    try {
                                        throw new Exception(databaseError.getMessage());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        try {
                            throw new Exception(databaseError.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try {
                    throw new Exception(databaseError.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //==============NOT FRIENDS=========================
                mProfileSendReqBtn.setEnabled(false);
                if (mCurrent_state.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootref.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");
                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootref.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request->" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }
                //===================================CANCAEL REQUEST==============================================
                if (mCurrent_state.equals("req_sent")) {
                    mFiriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFiriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }
                //=================================== REQUEST RECEIVED==============================================
                if (mCurrent_state.equals("req_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();

                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootref.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mCurrent_state = "Friends";
                                mProfileSendReqBtn.setText("UnFriend this Person");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });
                }
                //=============================UNFRIEND=======================================
                if (mCurrent_state.equals("Friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootref.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError==null){
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }
            }

        });
        //=============================DECLINE REQ============================================================,
        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map declineMap=new HashMap();
                declineMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id,null);
                declineMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid(),null);
                  mRootref.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                          if(mCurrent_state.equals("req_received")){
                              mCurrent_state="not_friends";
                              mProfileSendReqBtn.setText("Send Friend Request");

                              mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                              mProfileDeclineBtn.setEnabled(false);
                          }
                      }
                  });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrent_user!=null){
            mUsersRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrent_user!=null){
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }
}
