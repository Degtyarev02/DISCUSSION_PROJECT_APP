package com.example.messenger_project.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger_project.Messages;
import com.example.messenger_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText, receiver_username, senderMessageTime, receiverMessageTime;
        public CircleImageView receiverProfileImage;
        public LinearLayout receiverLayout, senderLayout;
        public ImageView messageSenderImage, messageReceiverImage;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            messageSenderImage = itemView.findViewById(R.id.sender_image_view_for_any_file);
            senderMessageTime = itemView.findViewById(R.id.sender_message_time);

            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiver_username = itemView.findViewById(R.id.receiver_username);
            receiverLayout = itemView.findViewById(R.id.Receiver_layout);
            messageReceiverImage = itemView.findViewById(R.id.receiver_image_view_for_any_file);
            receiverMessageTime = itemView.findViewById(R.id.receiver_messages_time);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild("image"))
                {
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.man_user).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
/*
        holder.senderMessageText.setVisibility(View.INVISIBLE);
        holder.senderMessageTime.setVisibility(View.INVISIBLE);
        holder.messageSenderImage.setVisibility(View.INVISIBLE);*/
        holder.senderLayout.setVisibility(View.INVISIBLE);

/*
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiver_username.setVisibility(View.INVISIBLE);
        holder.receiverMessageTime.setVisibility(View.INVISIBLE);
        holder.messageReceiverImage.setVisibility(View.INVISIBLE);*/

        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.receiverLayout.setVisibility(View.INVISIBLE);



        if(fromMessageType.equals("text"))
        {

            if(fromUserId.equals(messageSenderID))
            {
                holder.senderLayout.setVisibility(View.VISIBLE);

                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageTime.setText(messages.getTime());
            }
            else
            {

                /*holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiver_username.setVisibility(View.VISIBLE);*/
                holder.receiverLayout.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiver_username.setText(messages.getName());
                holder.receiverMessageTime.setText(messages.getTime());
            }
        }

        else if(fromMessageType.equals("image"))
        {
            if(fromUserId.equals(messageSenderID))
            {
                holder.senderLayout.setVisibility(View.VISIBLE);

                holder.senderMessageText.setText("Image");
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
                holder.senderMessageTime.setText(messages.getTime());
            }
            else
            {
                holder.receiverLayout.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setText("Image");
                holder.receiver_username.setText(messages.getName());

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);

                holder.receiverMessageTime.setText(messages.getTime());
            }
        }

    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

}
