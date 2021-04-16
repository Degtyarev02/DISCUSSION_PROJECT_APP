package com.example.messenger_project.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.messenger_project.Activities.ChatActivity;
import com.example.messenger_project.Contacts;
import com.example.messenger_project.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment
{
    private View privateChatView;
    private RecyclerView chatList;
    private ImageView emptyChats;

    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        emptyChats = privateChatView.findViewById(R.id.empty_chats);
        chatList = privateChatView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    chatList.setVisibility(View.INVISIBLE);
                    emptyChats.setVisibility(View.VISIBLE);
                }
                else
                {
                    chatList.setVisibility(View.VISIBLE);
                    emptyChats.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String userIDs = getRef(position).getKey();
                        final String[] retUserImage = {"default_image"};
                        usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (snapshot.exists())
                                {
                                    if(snapshot.hasChild("image"))
                                    {
                                        retUserImage[0] = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retUserImage[0]).into(holder.profImage);
                                    }
                                    final String retUserName = snapshot.child("name").getValue().toString();
                                    final String retUserStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(retUserName);
                                    holder.userStatus.setText("Last seen: " + "\n" + "Date " + " Time");

                                    holder.itemView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_Id", userIDs);
                                            chatIntent.putExtra("visit_user_name", retUserName);
                                            chatIntent.putExtra("visit_user_image", retUserImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_dispaly_layout, parent, false);
                        return new ChatsViewHolder(view);
                    }
                };

            chatList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
            chatList.setAdapter(adapter);
            adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profImage;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}