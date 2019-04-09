package com.example.nedadlipovac.lappitchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
private FirebaseUser mCurrent_user;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();

        mUsersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        //tool bar
        mToolBar = (Toolbar) findViewById(R.id.usersAppBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        mUsersList = (RecyclerView) findViewById(R.id.usersList);
         mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        //firebase
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrent_user=FirebaseAuth.getInstance().getCurrentUser();

    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrent_user!=null){
            mUsersRef.child("online").setValue("true");
        }

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUsersDatabase, Users.class)
                        .build();
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                                           holder.setNameAndStatus(model.getName(),model.getStatus());
                                           holder.setUserImage(model.getThumb_image(),getApplicationContext());
                                           final String userId=getRef(position).getKey();

                                               holder.mView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if(!mCurrent_user.getUid().equals(userId)) {
                                                           Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                                           profileIntent.putExtra("user_id", userId);
                                                           startActivity(profileIntent);
                                                       }
                                                       else{
                                                           Toast.makeText(UsersActivity.this,"It's your profile :) ",Toast.LENGTH_LONG).show();
                                                       }
                                                   }
                                               });


            }
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
       firebaseRecyclerAdapter.startListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setNameAndStatus(String name, String status) {
            TextView mUserNameView=mView.findViewById(R.id.usersSingleName);
            TextView mUserStatus=mView.findViewById(R.id.usersSingleStatus);
            mUserStatus.setText(status);
            mUserNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context applicationContext) {
            CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.userSingleImage);
            Picasso.with(applicationContext).load(thumb_image)
                    .placeholder(R.mipmap.ic_launcher_profile56)
                    .into(userImageView);
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
