package admin_jyb.musicplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import admin_jyb.musicplayer.R;
import admin_jyb.musicplayer.bean.MusicInfo;
import admin_jyb.musicplayer.service.MusicPlayService;
import admin_jyb.musicplayer.utils.Utils;



/**
 * Created by Admin-JYB on 2016/10/31.
 */
public class MusicPlayerActivity extends Activity{

    private ServiceConnection conn;
    private MusicPlayService playService;
    private Button pre;
    private Button next;
    private Button play;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private TextView tilte;
    private BroadcastReceiver receiver;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    updatePlayTime();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_musicplayer);
        initView();
        registerUpdateUiReceiver();
        connectService();
        initListener();
    }

    private void registerUpdateUiReceiver() {
        receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("stop_update_ui",false)){
                    handler.removeCallbacksAndMessages(null);
                }else {
                    MusicInfo musicInfo = (MusicInfo) intent.getSerializableExtra("musicInfo");
                    updateUi(musicInfo);
                }
            }
        };
        IntentFilter filter = new IntentFilter("update_ui_action");
        registerReceiver(receiver,filter);
    }

    private void updateUi(MusicInfo musicInfo) {
        if (musicInfo == null){
            return;
        }
        tilte.setText(musicInfo.title);
        seekBar.setMax(playService.getDuration());
        totalTime.setText(Utils.formatMillis(playService.getDuration()));
        updatePlayTime();
        updatePlayButton();
    }

    private void updatePlayTime() {
        currentTime.setText(Utils.formatMillis(playService.getCurrentPosition()));
        seekBar.setProgress(playService.getCurrentPosition());
        if (playService.isPlaying()){
            playService.sendNotification();
        }
        handler.removeCallbacksAndMessages(0);
        handler.sendEmptyMessageDelayed(0,50);
    }

    private void updatePlayButton() {
        if (playService.isPlaying()){
            play.setText("暂停");
        }else {
            play.setText("播放");
        }
    }

    private void initListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    playService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initView() {
        pre = (Button) findViewById(R.id.btn_pre);
        next = (Button) findViewById(R.id.btn_next);
        play = (Button) findViewById(R.id.btn_play);
        currentTime = (TextView) findViewById(R.id.tv_currenttime);
        totalTime = (TextView) findViewById(R.id.tv_totaltime);
        tilte = (TextView) findViewById(R.id.tv_title);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
    }

    private void connectService() {
        ArrayList<MusicInfo> musicInfolist =
                (ArrayList<MusicInfo>) getIntent().getSerializableExtra("list");
        int currentPosition = getIntent().getIntExtra("position", -1);
        int what = getIntent().getIntExtra("what",-1);
        Intent intent = new Intent(this, MusicPlayService.class);
        intent.putExtra("list",musicInfolist);
        intent.putExtra("position",currentPosition);
        intent.putExtra("what",what);
        startService(intent);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicPlayService.MyBinder myBinder = (MusicPlayService.MyBinder) service;
                playService = myBinder.playService;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, conn,BIND_AUTO_CREATE);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_pre:
                playService.pre();
                break;
            case R.id.btn_play:
                playService.playToggle();
                updatePlayButton();
                break;
            case R.id.btn_next:
                playService.next();
                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn != null){
            unbindService(conn);
        }
        if (receiver != null){
            unregisterReceiver(receiver);
        }
        handler.removeCallbacksAndMessages(null);
    }
}
