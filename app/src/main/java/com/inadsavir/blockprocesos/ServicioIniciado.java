package com.inadsavir.blockprocesos;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.security.Provider;

public class ServicioIniciado extends Service {

    MediaPlayer reproductor;

    public void onCreate(){
        super.onCreate();
        reproductor=MediaPlayer.create(this,R.raw.edsheran);
        reproductor.setLooping(true);
        reproductor.setVolume(100,100);
    }

    public int onStartCommand(Intent intent, int flags,int startId){
        reproductor.start();
        return START_STICKY;
    }

    public void onDestroy(){
        super.onDestroy();
        if(reproductor.isPlaying()) reproductor.stop();
        reproductor.release();
        reproductor=null;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
