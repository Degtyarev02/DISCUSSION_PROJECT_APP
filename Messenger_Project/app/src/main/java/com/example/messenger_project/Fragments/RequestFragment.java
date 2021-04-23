package com.example.messenger_project.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.Contacts;
import com.example.messenger_project.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class RequestFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myReqList;

    private DatabaseReference chatReqRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);
        myReqList = RequestsFragmentView.findViewById(R.id.chat_request_list);
        myReqList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatReqRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists())
                                {
                                    String type = snapshot.getValue().toString();

                                    if(type.equals("received"))
                                    {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("image"))
                                                {

                                                    final String requestUserImage = snapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserImage).into(holder.profImage);
                                                }

                                                final String requestUserName = snapshot.child("name").getValue().toString();
                                                final String requestUserStatus = snapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {

                                                        FlatDialog flatDialog = new FlatDialog(getContext());
                                                        flatDialog
                                                                .setTitle(requestUserName + " Wants to connect with you")
                                                                .setFirstButtonText("Accept")
                                                                .setSecondButtonText("Cancel")
                                                                .setBackgroundColor(Color.parseColor("#FFFFFF"))
                                                                .setFirstButtonColor(Color.parseColor("#2b2e4a"))
                                                                .setFirstButtonTextColor(Color.parseColor("#f0f0f0"))
                                                                .setSecondButtonColor(Color.parseColor("#903749"))
                                                                .setSecondButtonTextColor(Color.parseColor("#f0f0f0"))
                                                                .setTitleColor(Color.parseColor("#2b2e4a"))
                                                                .withFirstButtonListner(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view)
                                                                    {
                                                                        contactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                                .setValue("Saved")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    contactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                            .setValue("Saved")
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                            if(task.isSuccessful())
                                                                                            {
                                                                                                chatReqRef.child(currentUserID).child(list_user_id)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            chatReqRef.child(list_user_id).child(currentUserID)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                                {
                                                                                                                    if(task.isSuccessful())
                                                                                                                    {
                                                                                                                        Toasty.success(getContext(), "New Contact added", Toasty.LENGTH_SHORT).show();
                                                                                                                    }

                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                        flatDialog.dismiss();
                                                                    }
                                                                })
                                                                .withSecondButtonListner(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view)
                                                                    {
                                                                        chatReqRef.child(currentUserID).child(list_user_id)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            chatReqRef.child(list_user_id).child(currentUserID)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                Toasty.success(getContext(), "Contact deleted", Toasty.LENGTH_SHORT).show();
                                                                                                            }

                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                        flatDialog.dismiss();
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error)
                                            {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button requestSentButton = holder.itemView.findViewById(R.id.request_accept_btn);
                                        requestSentButton.setText("Req sent");
                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("image"))
                                                {

                                                    final String requestUserImage = snapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserImage).into(holder.profImage);
                                                }

                                                final String requestUserName = snapshot.child("name").getValue().toString();
                                                final String requestUserStatus = snapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {

                                                        FlatDialog flatDialog = new FlatDialog(getContext());
                                                        flatDialog
                                                                .setTitle("You have sent a request to " + requestUserName)
                                                                .setFirstButtonText("Cancel")
                                                                .setSecondButtonText("Close")
                                                                .setBackgroundColor(Color.parseColor("#FFFFFF"))
                                                                .setFirstButtonColor(Color.parseColor("#2b2e4a"))
                                                                .setFirstButtonTextColor(Color.parseColor("#f0f0f0"))
                                                                .setSecondButtonColor(Color.parseColor("#903749"))
                                                                .setSecondButtonTextColor(Color.parseColor("#f0f0f0"))
                                                                .setTitleColor(Color.parseColor("#2b2e4a"))
                                                                .withFirstButtonListner(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view)
                                                                    {
                                                                        chatReqRef.child(currentUserID).child(list_user_id)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            chatReqRef.child(list_user_id).child(currentUserID)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                Toasty.success(getContext(), "You have canceled chat request", Toasty.LENGTH_SHORT).show();
                                                                                                            }

                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
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
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error)
                                            {

                                            }
                                        });

                                    }
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
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_dispaly_layout, parent, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        myReqList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class  RequestViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profImage;
        Button acceptBtn, cancelBtn;

        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_cancel_btn);
            profImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}