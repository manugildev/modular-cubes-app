package com.manugildev.modularcubes.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.fragments.dummy.Messages;
import com.manugildev.modularcubes.ui.messages.MessagesRecyclerViewAdapter;

public class MessageFragment extends Fragment {

    private int mColumnCount = 1;
    private MessagesRecyclerViewAdapter mAdapter;
    RecyclerView recyclerView;

    public MessageFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MessageFragment newInstance() {

        return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MessagesRecyclerViewAdapter(Messages.ITEMS);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void addItem(String received) {
        if (mAdapter != null) {
            mAdapter.addItem(received);
            recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }

    }

}
