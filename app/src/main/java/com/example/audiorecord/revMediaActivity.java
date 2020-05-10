package com.example.audiorecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.audiorecord.recordFragment.postView2;

public class revMediaActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay= null;
    TextView list_title,postView,rerecView;
    ImageView playimage;
    SeekBar player_seekbar;
    private CircleLineVisualizer mVisualizer;
    private Handler seekbarHandler;
    private Runnable updateseekbar;
    private StorageReference mstorage;
    private ProgressDialog mprogress;
    FirebaseFirestore fstore ;
    String recordPath, recordfile;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rev_media);
        list_title = findViewById(R.id.list_title);
        list_title.setText(getIntent().getStringExtra("Filepalaying Name"));
        mVisualizer = findViewById(R.id.blob);
        postView = findViewById(R.id.postView);
        rerecView = findViewById(R.id.rerecView);
        playimage = findViewById(R.id.play_image_view);
        player_seekbar = findViewById(R.id.player_seekbar);
        mVisualizer.setDrawLine(true);
        mstorage = FirebaseStorage.getInstance().getReference();
        fstore = FirebaseFirestore.getInstance();
        mprogress = new ProgressDialog(this);
        recordPath = getIntent().getStringExtra("recordpath");
        recordfile = getIntent().getStringExtra("recordfile");
        fileToPlay = new File(recordPath+ "/" + recordfile);
        list_title.setText(recordfile);


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
                    }
                }
            }
        });
        rerecView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    stopAudio();
                    fileToPlay.delete();
                    postView2.setVisibility(View.INVISIBLE);
                    finish();
                }else{
                    fileToPlay.delete();
                    postView2.setVisibility(View.INVISIBLE);
                    finish();
                }
                if (mVisualizer != null)
                    mVisualizer.release();
            }
        });
        postView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    pauseAudio();
                    uploadfiles();
                }else{
                    uploadfiles();
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

    private void uploadfiles() {
        final String RecKey = UUID.randomUUID().toString().substring(0,16);
        final DocumentReference documentReference = fstore.collection("Recordinglist").document(RecKey);
        mprogress.setMessage("Uploading Audio.....");
        mprogress.setCancelable(false);
        mprogress.show();
        final StorageReference filepath = mstorage.child("Audio").child(recordfile);
        Uri uri = Uri.fromFile(new File(recordPath+ "/" + recordfile));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        final Map<String , Object> postmap =new HashMap<>();
                        postmap.put("RecTitle", recordfile);
                        postmap.put("Reckey", RecKey);
                        postmap.put("RecLink", downloadUrl.toString());
                        documentReference.set(postmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Your Audio is successfully posted", Toast.LENGTH_SHORT).show();
                                mprogress.dismiss();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                                mprogress.dismiss();
                            }
                        });

                    }
                });


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
        int audioSessionId = mediaPlayer.getAudioSessionId();
        updateRunnable();
        seekbarHandler.postDelayed(updateseekbar,0);
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
