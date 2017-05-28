package com.mobileapp.joyrhythm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.mobileapp.joyrhythm.R;

/**
 * Created by soojin on 2017-05-28.
 */

public class MainMusicService extends Service{
    public MediaPlayer mediaPlayer;


    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    public void onStart(Intent intent, int startId){
        Log.i("Music", "Service onStart()");
        super.onStart(intent,startId);
        mediaPlayer = MediaPlayer.create(this, R.raw.nine_point_eight);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
    }

    public void onDestroy(){
        Log.i("Music","Service on Destory()");
        super.onDestroy();
        mediaPlayer.pause();
        mediaPlayer.reset();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
            }
        });
    }


}
