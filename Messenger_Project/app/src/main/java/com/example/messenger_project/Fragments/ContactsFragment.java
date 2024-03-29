package com.example.messenger_project.Fragments;

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

public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactList;
    private DatabaseReference ContactsRef, usersRef;
    private FirebaseAuth myAuth;
    private String currentUserID;
    private ImageView emptyContacts;

    public ContactsFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactList = ContactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        emptyContacts = ContactsView.findViewById(R.id.empty_contacts);

        myAuth = FirebaseAuth.getInstance();
        currentUserID = myAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class).build();

        ContactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    myContactList.setVisibility(View.INVISIBLE);
                    emptyContacts.setVisibility(View.VISIBLE);
                }
                else
                {
                    myContactList.setVisibility(View.VISIBLE);
                    emptyContacts.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model)
            {
                String usersIds = getRef(position).getKey();
                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.hasChild("image"))
                        {
                            String profileImage = snapshot.child("image").getValue().toString();
                            String profileName = snapshot.child("name").getValue().toString();
                            String profileStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);
                            Picasso.get().load(profileImage).placeholder(R.drawable.man_user).into(holder.profImage);
                        }
                        else
                        {
                            String profileName = snapshot.child("name").getValue().toString();
                            String profileStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);
                        }
                        if(snapshot.child("userState").hasChild("State"))
                        {
                            String state = snapshot.child("userState").child("State").getValue().toString();
                            if(state.equals("online"))
                            {
                                holder.greenDotOnlineStatus.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                holder.greenDotOnlineStatus.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_display_layout, parent, false);
                return new ContactsViewHolder(view);
            }
        };

        myContactList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profImage;
        ImageView greenDotOnlineStatus;


        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profImage = itemView.findViewById(R.id.user_profile_image);
            greenDotOnlineStatus = itemView.findViewById(R.id.online_status_dot);
        }
    }


}