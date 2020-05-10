package com.example.audiorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.io.File;
import java.io.IOException;


public class mediaActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay= null;
    private File[] allFiles;
    TextView list_title;
    ImageView playimage;
    SeekBar player_seekbar;
    private CircleLineVisualizer mVisualizer;
    private Handler seekbarHandler;
    private Runnable updateseekbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        list_title = findViewById(R.id.list_title);
        list_title.setText(getIntent().getStringExtra("Filepalaying Name"));
        mVisualizer = findViewById(R.id.blob);
        playimage = findViewById(R.id.play_image_view);
        player_seekbar = findViewById(R.id.player_seekbar);
        mVisualizer.setDrawLine(true);
        String path = getApplication().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();
        int position  =  getIntent().getIntExtra("position", 0);
        Log.d("pos", position+"");
        fileToPlay = allFiles[position];


        if(isPlaying){
            stopAudio();
            playAudio(fileToPlay);

        }else{
            //playing audio
            playAudio(fileToPlay);
            //isPlaying =true;
        }
        playimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    pauseAudio();
                }else{
                    if(fileToPlay!=null){
                        resumeAudio();
                        //playAudio(fileToPlay);
                    }
                }
            }
        });
        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();

            }
        });

    }
    private void pauseAudio(){
        mediaPlayer.pause();
        isPlaying = false;
        playimage.setImageDrawable(getApplication().getResources().getDrawable(R.drawable.play_button));
        seekbarHandler.removeCallbacks(updateseekbar);


    }
    private void resumeAudio(){
        mediaPlayer.start();
        isPlaying = true;
        playimage.setImageDrawable(getApplication().getResources().getDrawable(R.drawable.pause));
        updateRunnable();
        seekbarHandler.postDelayed(updateseekbar,0);
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1)
            mVisualizer.setAudioSessionId(audioSessionId);


    }

    private void stopAudio() {
        isPlaying = false;
        playimage.setImageDrawable(getApplication().getResources().getDrawable(R.drawable.play_button));
        if (mVisualizer != null)
            mVisualizer.release();
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateseekbar);


    }

    private void playAudio(File fileToPlay) {
        isPlaying= true;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1)
                mVisualizer.setAudioSessionId(audioSessionId);
//            if (mVisualizer != null)
//                mVisualizer.hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
        playimage.setImageDrawable(getApplication().getResources().getDrawable(R.drawable.pause));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                playimage.setImageDrawable(getApplication().getResources().getDrawable(R.drawable.play_button));


            }
        });

        player_seekbar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();

        seekbarHandler.postDelayed(updateseekbar,0);

    }

    private void updateRunnable() {
        updateseekbar = new Runnable() {
            @Override
            public void run() {
                player_seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isPlaying){
            stopAudio();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if(isPlaying){
            stopAudio();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//
    }
}
