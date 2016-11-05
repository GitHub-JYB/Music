package admin_jyb.musicplayer.utils;

import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by Admin-JYB on 2016/11/3.
 */

public class Utils {

    public static CharSequence formatMillis(long duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();	// 清空日历里面的时间
        calendar.add(Calendar.MILLISECOND, (int) duration);
        int hourMillis = 1 * 60 * 60 * 1000;	// 一个小时对应的毫秒值
        boolean hasHour = duration / hourMillis > 0;
        CharSequence inFormat = hasHour ? "kk:mm:ss" : "mm:ss";
        return DateFormat.format(inFormat, calendar);
    }
}
