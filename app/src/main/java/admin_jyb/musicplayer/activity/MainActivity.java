package admin_jyb.musicplayer.activity;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

import admin_jyb.musicplayer.R;
import admin_jyb.musicplayer.adapter.MusicAdapter;
import admin_jyb.musicplayer.bean.MusicInfo;
import admin_jyb.musicplayer.service.MusicPlayService;

public class MainActivity extends Activity {

    private ArrayList<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.music_lv);
        initData();
        initListener();
    }

    private void initListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor  = (Cursor) parent.getItemAtPosition(position);
                cursor.moveToFirst();
                do {
                    musicInfoList.add(MusicInfo.fromCursor(cursor));
                }while (cursor.moveToNext());
                Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
                intent.putExtra("list",musicInfoList);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        AsyncQueryHandler queryHandler =
                new AsyncQueryHandler(getBaseContext().getContentResolver()){
                    @Override
                    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        listView.setAdapter(new MusicAdapter(getBaseContext(),cursor));
                    }
                };

        int token = 0;
        Object cookie = null;
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        String[] projection = {Media._ID, Media.TITLE, Media.ARTIST,Media.DATA};
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = Media.TITLE + " ASC";//升序排列
        queryHandler.startQuery(token,cookie,uri,projection,selection,selectionArgs,orderBy);
    }

}
