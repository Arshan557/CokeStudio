package com.arshan.cokestudio;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.concurrent.TimeUnit;


public class Player extends Activity {
    private ImageView next,pause,play,previous,stop,cover;
    private MediaPlayer mediaPlayer;
    private String AUDIO_PATH = null;
    private ProgressDialog pDialog;

    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    //private SeekBar seekbar;
    private int playbackPosition=0;
    private TextView songTitle, artists;

    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        SongsPojo data = (SongsPojo)getIntent().getExtras().getSerializable("DATA");
        Log.d("Detail",""+data.getSong()+","+data.getUrl()+","+data.getCover_image()+","+data.getArtists());

        next = (ImageView) findViewById(R.id.next);
        pause = (ImageView) findViewById(R.id.pause);
        play = (ImageView)findViewById(R.id.play);
        previous = (ImageView)findViewById(R.id.previous);
        stop = (ImageView) findViewById(R.id.stop);
        cover = (ImageView)findViewById(R.id.cover);

        songTitle = (TextView) findViewById(R.id.songTitle);
        artists = (TextView) findViewById(R.id.artists);

        AUDIO_PATH = data.getUrl();
        songTitle.setText(data.getSong());
        artists.setText("Artists: "+data.getArtists());
        Glide.with(Player.this).load(data.getCover_image()).into(cover);

        //seekbar = (SeekBar)findViewById(R.id.seekBar);
        //seekbar.setClickable(false);
        play.setEnabled(false);

        try {
            new Playsong().execute();
        } catch (Exception e) {
            Log.d("Exception",""+e.getLocalizedMessage());
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Playing",Toast.LENGTH_SHORT).show();
                try {
                    new Playsong().execute();
                } catch (Exception e) {
                    Log.d("Exception",""+e.getLocalizedMessage());
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Paused",Toast.LENGTH_SHORT).show();
                if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                    playbackPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                }
                pause.setEnabled(false);
                play.setEnabled(true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"Forwarded 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Backwarded 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"Forwarded 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Cannot backward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Stopped",Toast.LENGTH_SHORT).show();
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    playbackPosition = 0;
                }
                pause.setEnabled(false);
                play.setEnabled(true);
            }
        });
    }

    private void playAudio(String url) throws Exception
    {
        killMediaPlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare();
        mediaPlayer.start();

        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        /*if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        tx2.setText(String.format("%d :, %d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );

        tx1.setText(String.format("%d :, %d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);*/
        pause.setEnabled(true);
        play.setEnabled(false);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            /*tx1.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int)startTime);*/
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    private void killMediaPlayer() {
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class Playsong extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Player.this);
            pDialog.setMessage("Please wait while the song is loading..");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                playAudio(AUDIO_PATH);
            } catch (Exception e) {
                Log.d("Exception",""+e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

        }

    }
}
