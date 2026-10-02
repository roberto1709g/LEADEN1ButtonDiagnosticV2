package com.leaden1.buttondiagnostic;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.KeyEvent;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiagnosticMediaService extends MediaBrowserServiceCompat {
    private static final String CHANNEL_ID="leaden1_diagnostic";
    private static final int NOTIFICATION_ID=2002;
    private static final List<String> HISTORY=new ArrayList<>();
    private static String lastEvent="—";
    private static boolean sessionActive=false;
    private static Runnable uiCallback;
    private MediaSessionCompat mediaSession;

    public static synchronized void setUiCallback(@Nullable Runnable r){uiCallback=r;}
    public static synchronized void appendExternalEvent(String e){addHistory(e);}
    public static synchronized void clearHistory(){HISTORY.clear();lastEvent="—";notifyUi();}
    public static synchronized String getHistory(){StringBuilder s=new StringBuilder();for(String x:HISTORY)s.append(x).append("\n");return s.toString();}
    public static synchronized String getLastEvent(){return lastEvent;}
    public static synchronized boolean isSessionActive(){return sessionActive;}
    private static synchronized void addHistory(String e){lastEvent=e;HISTORY.add(0,e);if(HISTORY.size()>100)HISTORY.remove(HISTORY.size()-1);notifyUi();}
    private static void notifyUi(){if(uiCallback!=null)uiCallback.run();}
    private String now(){return new SimpleDateFormat("HH:mm:ss.SSS",Locale.getDefault()).format(new Date());}
    private void log(String e){addHistory(now()+" "+e);}

    @Override public void onCreate(){
        super.onCreate();
        createChannel();

        mediaSession=new MediaSessionCompat(this,"LEADEN1_V2");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,"LEADEN1 Button Diagnostic V2")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,"Diagnóstico Bluetooth")
                .build());

        long actions=PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_STOP |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_REWIND;

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,0f).build());

        mediaSession.setCallback(new MediaSessionCompat.Callback(){
            @Override public boolean onMediaButtonEvent(Intent intent){
                KeyEvent e=intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if(e!=null) log("onMediaButtonEvent: "+describe(e));
                else log("onMediaButtonEvent: SIN_KEY_EVENT");
                return super.onMediaButtonEvent(intent);
            }
            @Override public void onPlay(){log("CALLBACK onPlay()");setState(PlaybackStateCompat.STATE_PLAYING);}
            @Override public void onPause(){log("CALLBACK onPause()");setState(PlaybackStateCompat.STATE_PAUSED);}
            @Override public void onStop(){log("CALLBACK onStop()");setState(PlaybackStateCompat.STATE_STOPPED);}
            @Override public void onSkipToNext(){log("CALLBACK onSkipToNext()");}
            @Override public void onSkipToPrevious(){log("CALLBACK onSkipToPrevious()");}
            @Override public void onFastForward(){log("CALLBACK onFastForward()");}
            @Override public void onRewind(){log("CALLBACK onRewind()");}
        });

        mediaSession.setActive(true);
        sessionActive=mediaSession.isActive();

        Intent open=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,open,
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("LEADEN1 Button Diagnostic V2")
                .setContentText("Media Session activa — esperando GLASES")
                .setContentIntent(pi).setOngoing(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()))
                .build();

        startForeground(NOTIFICATION_ID,n);
        setSessionToken(mediaSession.getSessionToken());
        log("MEDIA_SESSION_ACTIVA");
    }

    private void setState(int state){
        PlaybackStateCompat old=mediaSession.getController().getPlaybackState();
        long a=old!=null?old.getActions():
                (PlaybackStateCompat.ACTION_PLAY|PlaybackStateCompat.ACTION_PAUSE|
                 PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(a).setState(state,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,0f).build());
    }

    private static String describe(KeyEvent e){
        int c=e.getKeyCode(); String n;
        switch(c){
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:n="MEDIA_PLAY_PAUSE";break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:n="MEDIA_PLAY";break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:n="MEDIA_PAUSE";break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:n="MEDIA_NEXT";break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:n="MEDIA_PREVIOUS";break;
            case KeyEvent.KEYCODE_MEDIA_STOP:n="MEDIA_STOP";break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:n="MEDIA_REWIND";break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:n="MEDIA_FAST_FORWARD";break;
            case KeyEvent.KEYCODE_MEDIA_RECORD:n="MEDIA_RECORD";break;
            case KeyEvent.KEYCODE_MEDIA_CLOSE:n="MEDIA_CLOSE";break;
            case KeyEvent.KEYCODE_MEDIA_EJECT:n="MEDIA_EJECT";break;
            case KeyEvent.KEYCODE_ASSIST:n="ASSIST";break;
            case KeyEvent.KEYCODE_VOLUME_UP:n="VOLUME_UP";break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:n="VOLUME_DOWN";break;
            default:n="KEYCODE_"+c;
        }
        return n+" action="+e.getAction()+" repeat="+e.getRepeatCount()+
                " flags=0x"+Integer.toHexString(e.getFlags())+
                " deviceId="+e.getDeviceId();
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CHANNEL_ID,"LEADEN1 Diagnóstico",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager m=getSystemService(NotificationManager.class);
            if(m!=null)m.createNotificationChannel(c);
        }
    }

    @Override public void onDestroy(){
        sessionActive=false;
        if(mediaSession!=null){mediaSession.setActive(false);mediaSession.release();}
        super.onDestroy();notifyUi();
    }

    @Override public BrowserRoot onGetRoot(String pkg,int uid,Bundle hints){
        return new BrowserRoot("LEADEN1_ROOT",null);
    }

    @Override public void onLoadChildren(String parent,Result<List<MediaBrowserCompat.MediaItem>> result){
        result.sendResult(new ArrayList<>());
    }
}
