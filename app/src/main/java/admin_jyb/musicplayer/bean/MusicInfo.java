package admin_jyb.musicplayer.bean;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by Admin-JYB on 2016/10/26.
 */

public class MusicInfo  implements Serializable{
    public String title;
    public String artist;
    public String data;

    public static MusicInfo fromCursor(Cursor cursor){
        MusicInfo musicInfo = new MusicInfo();
        musicInfo.data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        musicInfo.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        musicInfo.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        return musicInfo;
    }
}
