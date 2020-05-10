package com.example.audiorecord;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.czt.mp3recorder.MP3Recorder;

import com.shuyu.waveview.AudioWaveView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class recordFragment extends Fragment implements View.OnClickListener{
    private int PERMISSION_CODE = 45;
    private NavController navController;
    ImageButton record_listbtn;
    private ImageButton recButton;
    private boolean isrecording = false;
    private String recorPermission  = Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private String recordfile;
    Chronometer timer;
    TextView recstatus;
    String recordPath;
    ImageView pauseView;
    public static TextView postView2;

    //
    AudioWaveView audioWave;
    MP3Recorder mRecorder;
    TextView equView;

    private long pauseoffset;
    //



    public recordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        recButton = view.findViewById(R.id.record_btn);
        record_listbtn = view.findViewById(R.id.record_listbtn);
        record_listbtn.setOnClickListener(this);
        recButton.setOnClickListener(this);
        timer = view.findViewById(R.id.record_timer);
        recstatus = view.findViewById(R.id.record_filename);
        postView2 = view.findViewById(R.id.postview);
        pauseView = view.findViewById(R.id.pauseView);
        pauseView.setVisibility(View.INVISIBLE);
        pauseView.setOnClickListener(this);
        postView2.setEnabled(false);
        postView2.setVisibility(View.INVISIBLE);
        postView2.setOnClickListener(this);
        equView = view.findViewById(R.id.equView);
        audioWave = view.findViewById(R.id.audioWave);
        audioWave.setWaveColor(Color.parseColor("#ffff9a9e"));
        //

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_listbtn:
                if (isrecording){
                    Toast.makeText(getActivity(), "First stop the recording then navigate", Toast.LENGTH_SHORT).show();

                }else{
                    navController.navigate(R.id.action_recordFragment_to_listFragment);
                }

                break;

            case R.id.record_btn:
                if (isrecording){
                    //stop recording
                    stopRecording();
                    recButton.setImageDrawable(getResources().getDrawable(R.drawable.microphone));
                    isrecording= false;
                }else{
                    if(checkPermissions()){
                        startRecording();
                        recButton.setImageDrawable(getResources().getDrawable(R.drawable.microphoneon));
                        isrecording = true;

                    }

                }
                break;
            case R.id.postview:
                if(isrecording){
                    Toast.makeText(getActivity(), "you can't post", Toast.LENGTH_SHORT).show();

                }else{
                    uploadfiles();
                }
            case R.id.pauseView:
                resolvePause();
                break;


        }
    }
    private void resolvePause() {
        if (!isrecording)
            return;
        recButton.setEnabled(true);

        if (mRecorder.isPause()) {
            //stop chronometer
            timer.setBase(SystemClock.elapsedRealtime() - pauseoffset);
            timer.start();

            audioWave.setPause(false);
            mRecorder.setPause(false);
        } else {
            pauseoffset = SystemClock.elapsedRealtime() - timer.getBase();
            timer.stop();

            recButton.setEnabled(false);
            audioWave.setPause(true);
            mRecorder.setPause(true);
        }
    }


    private void uploadfiles() {
        Intent intent = new Intent(getActivity(), revMediaActivity.class);
        intent.putExtra("recordpath", recordPath);
        intent.putExtra("recordfile", recordfile);
        startActivity(intent);


    }

    private void startRecording() {
        pauseView.setVisibility(View.VISIBLE);
        equView.setVisibility(View.INVISIBLE);
        postView2.setEnabled(false);
        postView2.setVisibility(View.INVISIBLE);
        timer.setBase(SystemClock.elapsedRealtime() - pauseoffset);
        recstatus.setText("Start Recording");
        timer.start();
        recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy_MM__dd_hh_mm_ss", Locale.CANADA);
        Date now  = new Date();

        recordfile  ="Record_"+ dateFormat.format(now) +".mp3";
        mRecorder = new MP3Recorder(new File(recordPath+ "/" + recordfile));
        int offset = dip2px(getActivity(), 1);
        int size = getScreenWidth(getActivity()) / offset;
        mRecorder.setDataList(audioWave.getRecList(), size);
        try {
            mRecorder.start();
            audioWave.startView();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void stopRecording() {
        equView.setVisibility(View.INVISIBLE);
        pauseView.setVisibility(View.INVISIBLE);
        timer.setBase(SystemClock.elapsedRealtime());
        recstatus.setText("Stop Recording");
        timer.stop();
        pauseoffset = 0;
        mRecorder.stop();
        audioWave.stopView();
        postView2.setEnabled(true);
        postView2.setVisibility(View.VISIBLE);


    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), recorPermission)== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{recorPermission}, PERMISSION_CODE);
            return false;
        }
    }
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
    public static int dip2px(Context context, float dipValue) {
        float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * fontScale + 0.5f);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isrecording){
            isrecording= false;
            recButton.setImageDrawable(getResources().getDrawable(R.drawable.microphone));
            stopRecording();
        }
    }

}
