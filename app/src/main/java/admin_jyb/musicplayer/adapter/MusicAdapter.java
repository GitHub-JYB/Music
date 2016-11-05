package admin_jyb.musicplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


import admin_jyb.musicplayer.R;
import admin_jyb.musicplayer.bean.MusicInfo;

/**
 * Created by Admin-JYB on 2016/10/26.
 */
public class MusicAdapter extends CursorAdapter {
    public MusicAdapter(Context context, Cursor cursor) {
        super(context,cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_list, null);
        ViewHolder holder = new ViewHolder();
        holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
        holder.tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        MusicInfo musicInfo = MusicInfo.fromCursor(cursor);
        holder.tv_title.setText(musicInfo.title);
        holder.tv_artist.setText(musicInfo.artist);
    }

    static class ViewHolder {
        TextView tv_title;
        TextView tv_artist;
    }
}
