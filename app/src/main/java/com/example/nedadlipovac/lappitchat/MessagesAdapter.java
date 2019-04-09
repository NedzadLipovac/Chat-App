package com.example.nedadlipovac.lappitchat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    public MessagesAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;


    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single_layout, viewGroup, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        Messages c = messagesList.get(i);
        String messageType = c.getType();
        String from_user = c.getFrom();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                messageViewHolder.displayName.setText(name);

                Picasso.with(messageViewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.mipmap.ic_launcher_profile56)
                        .into(messageViewHolder.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (messageType.equals("text")) {
            if (from_user.equals(currentUserId)) {
                messageViewHolder.messageText.setBackgroundColor(Color.WHITE);
                messageViewHolder.messageText.setTextColor(Color.BLACK);
            } else {
                messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                messageViewHolder.messageText.setTextColor(Color.WHITE);

            }
            messageViewHolder.messageText.setText(c.getMessage());
            messageViewHolder.messageImage.setVisibility(View.INVISIBLE);
        } else {
              messageViewHolder.messageText.setVisibility(View.INVISIBLE);
              Picasso.with(messageViewHolder.profileImage.getContext()).load(c.getMessage())
                      .placeholder(R.mipmap.ic_launcher_profile56)
                      .into(messageViewHolder.messageImage);
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }
}
