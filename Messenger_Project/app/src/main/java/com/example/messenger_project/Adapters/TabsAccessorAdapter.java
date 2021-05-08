package com.example.messenger_project.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.messenger_project.Fragments.ChatsFragment;
import com.example.messenger_project.Fragments.ContactsFragment;
import com.example.messenger_project.Fragments.GroupsFragment;
import com.example.messenger_project.Fragments.RequestFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0: return new ChatsFragment();
            case 1: return new GroupsFragment();
            case 2: return new ContactsFragment();
            case 3: return new RequestFragment();
            default: return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0: return "Chats";
            case 1: return "Groups";
            case 2: return "Friends";
            case 3: return "Requests";
            default: return null;
        }
    }
}
