package admin_jyb.musicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.util.ArrayList;

import admin_jyb.musicplayer.R;
import admin_jyb.musicplayer.activity.MusicPlayerActivity;
import admin_jyb.musicplayer.bean.MusicInfo;



/**
 * Created by Admin-JYB on 2016/10/31.
 */
public class MusicPlayService extends Service{

    private ArrayList<MusicInfo> musicInfos;
    private MusicInfo musicInfo;
    private int position = -1;
    private MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int what = intent.getIntExtra("what",-1);
        switch (what){
            case 1:
                sendUpdateUiBroadcast();
                break;
            case 2:
                pre();
                break;
            case 3:
                next();
                break;
            default:
                musicInfos = (ArrayList<MusicInfo>) intent.getSerializableExtra("list");
                if (position == intent.getIntExtra("position",-1)){
                    sendUpdateUiBroadcast();
                    if (!isPlaying()){
                        playToggle();
                    }
                }else {
                    position = intent.getIntExtra("position", -1);
                    openMusic();
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendUpdateUiBroadcast() {
        Intent intent = new Intent("update_ui_action");
        intent.putExtra("musicInfo",musicInfo);
        sendBroadcast(intent);
    }

    private void release() {
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer!= null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    private void openMusic() {
        if (musicInfos == null || musicInfos.isEmpty() || position == -1){
            return;
        }
        sendStopUpdateUIBroadcast();
        musicInfo = musicInfos.get(position);

        //其他音频视频停止播放
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);

        release();
        new Handler().postAtTime(new Runnable() {
            @Override
            public void run() {
                initMediaPlay();
            }
        },300);
    }

    private void sendStopUpdateUIBroadcast() {
        Intent intent = new Intent("update_ui_action");
        intent.putExtra("stop_update_ui",true);
        sendBroadcast(intent);
    }

    private void initMediaPlay() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playToggle();
                    sendUpdateUiBroadcast();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    next();
                }
            });
            mediaPlayer.setDataSource(this, Uri.parse(musicInfo.data));
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null){
            mediaPlayer.seekTo(progress);
        }
    }

    public void pre() {
        if (position != 0){
            position--;
        }else position = musicInfos.size() - 1;
        openMusic();
    }

    public void playToggle() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                notificationManager.cancel(1);
            }else {
                mediaPlayer.start();
                sendNotification();
            }
        }
    }

    public void sendNotification() {
        int icon = R.drawable.music_default_bg;
        Notification.Builder builder = new Notification.Builder(this);
        CharSequence tickerText = "当前正在播放: " + musicInfo.title;
        long when  = System.currentTimeMillis();
        CharSequence contentTitle = musicInfo.title;
        CharSequence contentText = musicInfo.artist;
        PendingIntent contentIntent = getActivityPendingIntent(1);
        builder.setSmallIcon(icon)              //通知图标
                .setTicker(tickerText)          //通知提示文本
                .setWhen(when)                  //通知时间
                .setOngoing(true)               //让通知左右滑的时候无法取消通知
                .setContentTitle(contentTitle)  //通知显示的标题
                .setContentText(contentText)    //通知内容
                .setContentIntent(contentIntent)//点击通知时开启的Intent
                .setContent(getRemoteViews());  //自定义布局通知栏的布局
        Notification notification = builder.build();
        notificationManager.notify(1,notification);

    }

    private PendingIntent getActivityPendingIntent(int i) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra("what",i);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private RemoteViews getRemoteViews() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setProgressBar(R.id.progress,getDuration(),getCurrentPosition(),false);
        remoteViews.setOnClickPendingIntent(R.id.btn_pre,getServicePendingIntent(2));
        remoteViews.setOnClickPendingIntent(R.id.btn_next,getServicePendingIntent(3));
        remoteViews.setOnClickPendingIntent(R.id.ll_root,getActivityPendingIntent(1));
        return remoteViews;
    }

    private PendingIntent getServicePendingIntent(int i) {
        Intent intent = new Intent(this, MusicPlayService.class);
        intent.putExtra("what",i);
        PendingIntent contentIntent =
                PendingIntent.getService(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    public void next() {
        if (position != musicInfos.size() -1 ){
            position++;
        }else position = 0;
        openMusic();
    }

    public int getDuration() {
        if (mediaPlayer != null){
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public class MyBinder extends Binder {
        public MusicPlayService playService = MusicPlayService.this;
    }
}
