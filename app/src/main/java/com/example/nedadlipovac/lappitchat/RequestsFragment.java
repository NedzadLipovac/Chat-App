package com.example.nedadlipovac.lappitchat;


import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private RecyclerView mReqList;
    private DatabaseReference mReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mReqList = mMainView.findViewById(R.id.req_list);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
            mReqDatabase.keepSynced(true);
        }
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsDatabase.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mReqList.setLayoutManager(linearLayoutManager);
        mReqList.setHasFixedSize(true);
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            FirebaseRecyclerOptions<Req> options =
                    new FirebaseRecyclerOptions.Builder<Req>()
                            .setQuery(mReqDatabase, Req.class).build();
            FirebaseRecyclerAdapter<Req, ReqViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Req, ReqViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final ReqViewHolder holder, int position, @NonNull Req model) {
                   final String list_user_id = getRef(position).getKey();

                    mReqDatabase.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                         // Toast.makeText(getContext(), "onChildAddedREq funkcija ", Toast.LENGTH_LONG).show();
                            final String key = dataSnapshot.getKey();
                            mReqDatabase.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final String type = dataSnapshot.child("request_type").getValue().toString();
                                    Log.i("ON_Child_TEST", type + "->" + key);
                                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            String thumb=dataSnapshot.child("thumb_image").getValue().toString();
                                            if (type.equals("received")) {
                                                Log.i("TEST_MY_34", name+"--"+key);
                                                Log.i("TEST_MY_35", name+"--"+list_user_id);

                                                holder.setName(name);
                                                holder.setUserPicture(thumb,getContext());

                                                holder.mMainView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent profileintent=new Intent(getContext(),ProfileActivity.class);
                                                        profileintent.putExtra("user_id",list_user_id);
                                                        startActivity(profileintent);
                                                    }
                                                });

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


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

                @NonNull
                @Override
                public ReqViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.single_request_layout, viewGroup, false);

                    return new ReqViewHolder(view);
                }
            };
            mReqList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
        }
    }
    public class ReqViewHolder extends RecyclerView.ViewHolder {
        View mMainView;

        public ReqViewHolder(@NonNull View itemView) {
            super(itemView);
            mMainView = itemView;
        }

        public void setName(String userName) {
            TextView userNameView = (TextView) mMainView.findViewById(R.id.usersSingleReqName);
            userNameView.setText(userName);
        }

        public void setUserPicture(String thumb, Context context) {
            CircleImageView iv=(CircleImageView)mMainView.findViewById(R.id.userSingleReqImage);
            Picasso.with(context).load(thumb).placeholder(R.mipmap.ic_launcher_profile56).into(iv);
        }
    }
}
