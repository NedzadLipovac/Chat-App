package com.example.nedadlipovac.lappitchat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private RecyclerView mConvList;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabse;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mMainView = inflater.inflate(R.layout.fragment_chats, container, false);


            mConvList = mMainView.findViewById(R.id.conv_list);
            mAuth = FirebaseAuth.getInstance();
            if (mAuth!= null ) {
                if(mAuth.getCurrentUser()!=null) {
                    mCurrent_user_id = mAuth.getInstance().getCurrentUser().getUid();
                    mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
                    mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
                    mConvDatabase.keepSynced(true);
                }
            }


        mUsersDatabse = FirebaseDatabase.getInstance().getReference().child("Users");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        //Log.d("AUTH_ID",mAuth.getCurrentUser().getUid());
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()!= null) {

        Query conversatioQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(mConvDatabase, Conv.class)
                        .build();


        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {
                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data, model.isSeen());
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

                mUsersDatabse.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        holder.setName(userName);
                        holder.setUserImage(userThumb, getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);
                return new ConvViewHolder(view);
            }
        };
        mConvList.setAdapter(firebaseConvAdapter);
        firebaseConvAdapter.startListening();

    }
}
//
    public static class ConvViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String data, boolean seen) {
            TextView userStatusView = (TextView) mView.findViewById(R.id.usersSingleStatus);
            userStatusView.setText(data);
            if (!seen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);

            }

        }

        public void setUserImage(String userThumb, Context context) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userSingleImage);
            Picasso.with(context).load(userThumb).placeholder(R.mipmap.ic_launcher_profile56).into(userImageView);
        }

        public void setUserOnline(String userOnline) {
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.userSingleOnline);
            if (userOnline.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

        public void setName(String userName) {
            TextView userNameView = (TextView) mView.findViewById(R.id.usersSingleName);
            userNameView.setText(userName);
        }
    }







}
