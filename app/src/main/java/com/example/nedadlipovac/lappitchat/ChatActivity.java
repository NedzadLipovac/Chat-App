package com.example.nedadlipovac.lappitchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private String mUserName;
    private Toolbar mChatToolBar;
    private DatabaseReference mRootRef;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private DatabaseReference mUsersDatabase;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayOut;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mlayoutManager;
    private MessagesAdapter mAdapter;
    public static final int TOTAL_ITEMS_TO_LOAD = 10;
    public int mCurrentPage = 1;
     private int itemPos=0;
     private String mLastKey="";
     private String mPrevKey="";

    public static final Integer GALERY_PICKER = 1;
    private StorageReference mImageStorageReference;
    //Progress
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageStorageReference=FirebaseStorage.getInstance().getReference();

        setContentView(R.layout.activity_chat);
        mChatUser = getIntent().getStringExtra("user_id");
        mUserName = getIntent().getStringExtra("name");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
//my custom action bar
        mChatToolBar = findViewById(R.id.chatAppBar);
        setSupportActionBar(mChatToolBar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        actionBar.setDisplayShowCustomEnabled(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chatcustombar, null);
        actionBar.setCustomView(action_bar_view);
        mTitleView = (TextView) findViewById(R.id.chatCustomName);
        mLastSeenView = (TextView) findViewById(R.id.chatCutomLastSeen);
        mProfileImage = (CircleImageView) findViewById(R.id.customBarImage);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        mTitleView.setText(mUserName);

        mAdapter = new MessagesAdapter(messagesList);

        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayOut = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mlayoutManager = new LinearLayoutManager(this);
        mMessagesList.setLayoutManager(mlayoutManager);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();
// add last seen and name to appbar
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String name=dataSnapshot.child("name").getValue().toString();
                if (online.equals("true")) {
                    mLastSeenView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeen = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeen);
                }
                setUserImage(image, getApplicationContext());
                mTitleView.setText(name);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        //work with refresher to load more messages
        mRefreshLayOut.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();

            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent, "SELECT IMAGE"), GALERY_PICKER);
            }
        });
    }
//========================= SENDING IMAGE=============================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALERY_PICKER && resultCode==RESULT_OK){
            Uri imageUri=data.getData();
            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;
            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mChatUser).child(mCurrentUserId).push();
            final String push_id = user_message_push.getKey();
            final StorageReference filePath=mImageStorageReference.child("message_images").child(push_id+".jpg");


            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                          if(task.isSuccessful()){
                              filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri uri) {
                                      Map messageMap = new HashMap();

                                      messageMap.put("message", uri.toString());
                                      messageMap.put("seen", false);
                                      messageMap.put("type", "image");
                                      messageMap.put("time", ServerValue.TIMESTAMP);
                                      messageMap.put("from", mCurrentUserId);

                                      Map massageUserMap = new HashMap();
                                      massageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                      massageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                                      mChatMessageView.setText("");
                                      mRootRef.updateChildren(massageUserMap, new DatabaseReference.CompletionListener() {
                                          @Override
                                          public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                              if (databaseError != null) {
                                                  Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                              }
                                          }
                                      });
                                  }
                              });
                          }
                }
            });


        }

    }



    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                 String messageKey=dataSnapshot.getKey();
                 if(!mPrevKey.equals(messageKey)){
                     messagesList.add(itemPos++,message);
                 }
                      else {
                     mPrevKey=mLastKey;
                 }
                if(itemPos==1){
                    mLastKey=messageKey;
                }

                Log.d("TOTALKEYS","Last KEY "+mLastKey+"--- PrevKey :"+mPrevKey);
                mAdapter.notifyDataSetChanged();
                mRefreshLayOut.setRefreshing(false);
                mlayoutManager.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                 itemPos++;
                if(itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayOut.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setUserImage(String thumb_image, Context applicationContext) {

        Picasso.with(applicationContext).load(thumb_image)
                .placeholder(R.mipmap.ic_launcher_profile56)
                .into(mProfileImage);
    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;
            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mChatUser).child(mCurrentUserId).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();

            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map massageUserMap = new HashMap();
            massageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            massageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
            mChatMessageView.setText("");
            mRootRef.updateChildren(massageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });


        }

    }
}
