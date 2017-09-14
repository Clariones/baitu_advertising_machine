package com.skynet.adplayer.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
    public static int getTimeSecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        return hour * 60 * 60 + minute * 60 + second;
    }

    /**
     * Datetime： 2017-12-31 12:30:00
     * Date ： 2017-12-13
     * Time ： 12:30:00
     * @param curTimeSec
     * @param startTimeSec
     * @param endTimeSec
     * @return
     */
    public static boolean inTimeRange(int curTimeSec, int startTimeSec, int endTimeSec) {
        if (startTimeSec <= endTimeSec){
            return curTimeSec >= startTimeSec && endTimeSec >= curTimeSec;
        }
        return curTimeSec >= startTimeSec || curTimeSec <= endTimeSec;
    }
}