package com.example.nedadlipovac.lappitchat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private DatabaseReference mFriendsDataBase;
    private DatabaseReference mUsersDataBase;


    private RecyclerView mFirendsList;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;
    private String g;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFirendsList = (RecyclerView) mMainView.findViewById(R.id.friendsList);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDataBase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDataBase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDataBase.keepSynced(true);
        mFirendsList.setHasFixedSize(true);
        mFirendsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsDataBase, Friends.class)
                        .build();
        FirebaseRecyclerAdapter<Friends, FrinedsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FrinedsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FrinedsViewHolder holder, int position, @NonNull final Friends model) {

                final String list_user_id = getRef(position).getKey();
                mUsersDataBase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String thumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = (String) dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }

                        holder.setName(name);
                        holder.setUserImage(thumb, getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if (which == 1) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("name", name);

                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.setDate(model.getDate());
            }

            @NonNull
            @Override
            public FrinedsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);
                return new FrinedsViewHolder(view);
            }
        };
        mFirendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FrinedsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FrinedsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView userNameView = mView.findViewById(R.id.usersSingleStatus);
            userNameView.setText(date);
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.usersSingleName);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context applicationContext) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userSingleImage);
            Picasso.with(applicationContext).load(thumb_image)
                    .placeholder(R.mipmap.ic_launcher_profile56)
                    .into(userImageView);
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.userSingleOnline);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);

            }
        }
    }
}
