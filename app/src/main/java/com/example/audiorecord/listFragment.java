package com.example.audiorecord;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;


public class listFragment extends Fragment implements AudioListAdapter.onItemListClick{
    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;



    public listFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioList = view.findViewById(R.id.audio_list);
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();

        audioListAdapter = new AudioListAdapter(allFiles, this);
        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);


    }

    @Override
    public void onClickListener(File file, int position) {
        Intent intent = new Intent(getActivity(), mediaActivity.class);
        //intent.putExtra("Filepalaying", file.getAbsolutePath());
        intent.putExtra("position", position);
        intent.putExtra("Filepalaying Name", file.getName());
        Log.d("Filepalaying Name", file.getName()+ position);
        startActivity(intent);
    }
}
