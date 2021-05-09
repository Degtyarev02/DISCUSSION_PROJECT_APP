package com.example.messenger_project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.Activities.GroupChatActivity;
import com.example.messenger_project.Activities.MainActivity;
import com.example.messenger_project.Activities.ShowImageActivity;
import com.example.messenger_project.Messages;
import com.example.messenger_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private boolean zoomOut = false;
    private Context mCon;
    private String groupName;

    public MessageAdapter(Context c, List<Messages> userMessagesList) {
        mCon = c;
        this.userMessagesList = userMessagesList;
    }

    public MessageAdapter(Context c, List<Messages> userMessagesList, String groupName) {
        mCon = c;
        this.groupName = groupName;
        this.userMessagesList = userMessagesList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText,
                receiver_username, senderMessageTime, receiverMessageTime,
                senderImageTime, receiverImageTime;
        public CircleImageView receiverProfileImage;
        public LinearLayout receiverLayout, senderLayout, senderImageMessageLayout, receiverImageMessageLayout;
        public ImageView messageSenderImage, messageReceiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            messageSenderImage = itemView.findViewById(R.id.sender_image_view_for_any_file);
            senderMessageTime = itemView.findViewById(R.id.sender_message_time);

            senderImageMessageLayout = itemView.findViewById(R.id.for_sender_image_layout);
            senderImageTime = itemView.findViewById(R.id.sender_image_message_time);

            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiver_username = itemView.findViewById(R.id.receiver_username);
            receiverLayout = itemView.findViewById(R.id.Receiver_layout);
            messageReceiverImage = itemView.findViewById(R.id.receiver_image_view_for_any_file);
            receiverMessageTime = itemView.findViewById(R.id.receiver_messages_time);

            receiverImageMessageLayout = itemView.findViewById(R.id.for_receiver_Image_layout);
            receiverImageTime = itemView.findViewById(R.id.receiver_image_message_time);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")) {
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.man_user).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.senderImageMessageLayout.setVisibility(View.GONE);
        holder.senderLayout.setVisibility(View.GONE);

        holder.receiverImageMessageLayout.setVisibility(View.GONE);

        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.receiverLayout.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {

            if (fromUserId.equals(messageSenderID)) {
                holder.senderLayout.setVisibility(View.VISIBLE);

                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageTime.setText(messages.getTime());
            } else {

                /*holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiver_username.setVisibility(View.VISIBLE);*/
                holder.receiverLayout.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiver_username.setText(messages.getName());
                holder.receiverMessageTime.setText(messages.getTime());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserId.equals(messageSenderID)) {
                holder.senderImageMessageLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
                holder.senderImageTime.setText(messages.getTime());

                holder.messageSenderImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mCon, ShowImageActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("image", messages.getMessage());
                        mCon.startActivity(i);
                    }
                });
            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImageMessageLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);
                holder.receiverImageTime.setText(messages.getTime());

                holder.messageReceiverImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mCon, ShowImageActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("image", messages.getMessage());
                        mCon.startActivity(i);
                    }
                });
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserId.equals(messageSenderID)) {
                holder.senderImageMessageLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.document).into(holder.messageSenderImage);
                holder.senderImageTime.setText(messages.getFileName() + "   " + messages.getTime());

                holder.messageSenderImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImageMessageLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.document).into(holder.messageReceiverImage);
                holder.receiverImageTime.setText(messages.getFileName() + "   " + messages.getTime());

                holder.messageReceiverImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }


        if (fromUserId.equals(messageSenderID)) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                            if (mCon.getClass() != GroupChatActivity.class) {
                                FlatDialog flatDialog = new FlatDialog(holder.itemView.getContext());
                                flatDialog
                                        .setTitle("Delete message?")
                                        .setFirstButtonText("Delete for me")
                                        .setSecondButtonText("Delete for everyone")
                                        .setThirdButtonText("Cancel")

                                        .setBackgroundColor(mCon.getResources().getColor(R.color.white))

                                        .setFirstButtonColor(mCon.getResources().getColor(R.color.Gray))
                                        .setSecondButtonColor(mCon.getResources().getColor(R.color.DarkBlue))
                                        .setThirdButtonColor(mCon.getResources().getColor(R.color.DarkRed))

                                        .setFirstButtonTextColor(mCon.getResources().getColor(R.color.blackyGray))
                                        .setSecondButtonTextColor(mCon.getResources().getColor(R.color.whity_gray))
                                        .setThirdButtonTextColor(mCon.getResources().getColor(R.color.whity_gray))

                                        .setTitleColor(mCon.getResources().getColor(R.color.DarkBlue))
                                        .withFirstButtonListner(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                deleteSentMessages(position, holder);
                                                flatDialog.dismiss();
                                            }
                                        })
                                        .withSecondButtonListner(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                deleteMessageForEveryOne(position, holder);
                                                flatDialog.dismiss();
                                            }
                                        })
                                        .withThirdButtonListner(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                flatDialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else {
                                FlatDialog flatDialog = new FlatDialog(holder.itemView.getContext());
                                flatDialog
                                        .setTitle("Delete message?")
                                        .setFirstButtonText("Delete")
                                        .setSecondButtonText("Cancel")

                                        .setBackgroundColor(mCon.getResources().getColor(R.color.white))

                                        .setFirstButtonColor(mCon.getResources().getColor(R.color.Gray))
                                        .setSecondButtonColor(mCon.getResources().getColor(R.color.DarkBlue))

                                        .setFirstButtonTextColor(mCon.getResources().getColor(R.color.blackyGray))
                                        .setSecondButtonTextColor(mCon.getResources().getColor(R.color.whity_gray))

                                        .setTitleColor(mCon.getResources().getColor(R.color.DarkBlue))
                                        .withFirstButtonListner(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                deleteGroupMessage(position, holder);
                                                flatDialog.dismiss();
                                            }
                                        })
                                        .withSecondButtonListner(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                flatDialog.dismiss();
                                            }
                                        }).show();
                            }
                    return true;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    FlatDialog flatDialog = new FlatDialog(holder.itemView.getContext());
                    flatDialog
                            .setTitle("Delete message?")
                            .setFirstButtonText("Delete")
                            .setSecondButtonText("Cancel")

                            .setBackgroundColor(mCon.getResources().getColor(R.color.white))

                            .setFirstButtonColor(mCon.getResources().getColor(R.color.Gray))
                            .setSecondButtonColor(mCon.getResources().getColor(R.color.DarkBlue))

                            .setFirstButtonTextColor(mCon.getResources().getColor(R.color.blackyGray))
                            .setSecondButtonTextColor(mCon.getResources().getColor(R.color.whity_gray))

                            .setTitleColor(mCon.getResources().getColor(R.color.DarkBlue))
                            .withFirstButtonListner(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteReceiveMessages(position, holder);
                                    flatDialog.dismiss();
                                }
                            })
                            .withSecondButtonListner(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    flatDialog.dismiss();
                                }
                            })
                            .show();

                    return true;
                }
            });
        }

    }

    private void deleteGroupMessage(final int position, MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Groups")
                .child(groupName)
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT).show();
                    Intent i = new Intent(holder.itemView.getContext(), MainActivity.class);
                    holder.itemView.getContext().startActivity(i);
                } else
                    Toasty.error(holder.itemView.getContext(), "Something went wrong", Toasty.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSentMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT).show();
                    Intent i = new Intent(holder.itemView.getContext(), MainActivity.class);
                    holder.itemView.getContext().startActivity(i);
                } else
                    Toasty.error(holder.itemView.getContext(), "Something went wrong", Toasty.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteReceiveMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT).show();
                    Intent i = new Intent(holder.itemView.getContext(), MainActivity.class);
                    holder.itemView.getContext().startActivity(i);
                } else
                    Toasty.error(holder.itemView.getContext(), "Something went wrong", Toasty.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT).show();
                                Intent i = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(i);
                            } else
                                Toasty.error(holder.itemView.getContext(), "Something went wrong", Toasty.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
