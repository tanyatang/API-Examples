package io.agora.api.example;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.api.example.annotation.Example;
import io.agora.api.example.common.adapter.ExampleSection;
import io.agora.api.example.common.model.Examples;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MainFragment extends Fragment {
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MainFragment newInstance(int columnCount) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            SectionedRecyclerViewAdapter sectionedAdapter = new SectionedRecyclerViewAdapter();
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Channel), Examples.ITEM_MAP.get(Examples.Channel), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Audio), Examples.ITEM_MAP.get(Examples.Audio), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Video), Examples.ITEM_MAP.get(Examples.Video), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Player), Examples.ITEM_MAP.get(Examples.Player), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Recorder), Examples.ITEM_MAP.get(Examples.Recorder), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Cloud), Examples.ITEM_MAP.get(Examples.Cloud), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.MetaData), Examples.ITEM_MAP.get(Examples.MetaData), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Device), Examples.ITEM_MAP.get(Examples.Device), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.External), Examples.ITEM_MAP.get(Examples.External), mListener));
            sectionedAdapter.addSection(new ExampleSection(getGroupName(Examples.Network), Examples.ITEM_MAP.get(Examples.Network), mListener));
            recyclerView.setAdapter(sectionedAdapter);
        }
        return view;
    }

    private String getGroupName(String group) {
        switch (group) {
            case Examples.Channel:
                return getString(R.string.group_channel);
            case Examples.Audio:
                return getString(R.string.group_audio);
            case Examples.Video:
                return getString(R.string.group_video);
            case Examples.Player:
                return getString(R.string.group_player);
            case Examples.Recorder:
                return getString(R.string.group_recorder);
            case Examples.Cloud:
                return getString(R.string.group_cloud);
            case Examples.MetaData:
                return getString(R.string.group_metadata);
            case Examples.Device:
                return getString(R.string.group_device);
            case Examples.External:
                return getString(R.string.group_external);
            case Examples.Network:
                return getString(R.string.group_network);
        }
        return group;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Example item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.setting) {
            startActivity(new Intent(requireContext(), SettingActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
