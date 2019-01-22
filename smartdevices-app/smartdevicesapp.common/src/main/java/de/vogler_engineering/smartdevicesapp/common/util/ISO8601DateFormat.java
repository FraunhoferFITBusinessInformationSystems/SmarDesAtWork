/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ISO8601DateFormat extends DateFormat {

//    private final static String regex = "((?<year>\\d{4})-(?<month>\\d\\d)-(?<day>\\d\\d)" +
//            "T(?<hour>\\d\\d):(?<minute>\\d\\d):(?<second>\\d\\d))" +
//            "(\\.(?<milis>\\d+))?(((?<tzsign>[+-])(?<tzhour>\\d\\d)(:?)(?<tzminute>\\d\\d))" +
//            "|(?<tzz>Z))?";
//    private final static String regex = "((\\d{4})-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d))(\\.(\\d+))?((([+-])(\\d\\d)(:?)(\\d\\d))|(Z))?";
//    private final int GRP_YEAR = 2;
//    private final int GRP_MONTH = 3;
//    private final int GRP_DAY = 4;
//    private final int GRP_HOUR = 5;
//    private final int GRP_MINUTE = 6;
//    private final int GRP_SECOND = 7;
//    private final int GRP_MS = 9;
//    private final int GRP_TZ_SIGN = 12;
//    private final int GRP_TZ_Z = 16;
//    private final int GRP_TZ_HOUR = 13;
//    private final int GRP_TZ_MINUTE = 15;

    private final static String regex = "((\\d{4})-(\\d\\d)-(\\d\\d)(T(\\d\\d)(:(\\d\\d)(:(\\d\\d)(\\.(\\d+))?)?)?)?)((([+-])(\\d\\d)(:?)(\\d\\d))|(Z))?";
    private final int GRP_YEAR = 2;
    private final int GRP_MONTH = 3;
    private final int GRP_DAY = 4;
    private final int GRP_HOUR_PRESENT = 5;
    private final int GRP_HOUR = 6;
    private final int GRP_MINUTE_PRESENT = 7;
    private final int GRP_MINUTE = 8;
    private final int GRP_SECOND_PRESENT = 9;
    private final int GRP_SECOND = 10;
    private final int GRP_MS = 12;
    private final int GRP_TZ_SIGN = 15;
    private final int GRP_TZ_Z = 19;
    private final int GRP_TZ_HOUR = 16;
    private final int GRP_TZ_MINUTE = 18;

    private final Pattern pattern;

    public ISO8601DateFormat() {
        this(TimeZone.getDefault());
    }

    public ISO8601DateFormat(TimeZone timeZone) {
        this.calendar = new GregorianCalendar(timeZone);
        this.calendar.clear();
        this.pattern = Pattern.compile(regex);
    }

    static private long   lastTime;
    static private char[] lastTimeString = new char[20];

    /**
     Appends a date in the format "YYYY-mm-dd'T'HH:mm:ss.SSSZ"
     to sbuf. For example: "1999-11-27T15:49:37.459Z".

     @param sbuf the StringBuffer to write to
     */
    public
    StringBuffer format(Date date, StringBuffer sbuf, FieldPosition fieldPosition) {
        long now = date.getTime();
        int millis = (int)(now % 1000);

        if ((now - millis) != lastTime) {
            // We reach this point at most once per second
            // across all threads instead of each time format()
            // is called. This saves considerable CPU time.


            calendar.setTime(date);

            int start = sbuf.length();

            int year =  calendar.get(Calendar.YEAR);
            sbuf.append(year);

            String month;
            switch(calendar.get(Calendar.MONTH)) {
                case Calendar.JANUARY: month = "-01-"; break;
                case Calendar.FEBRUARY: month = "-02-";  break;
                case Calendar.MARCH: month = "-03-"; break;
                case Calendar.APRIL: month = "-04-";  break;
                case Calendar.MAY: month = "-05-"; break;
                case Calendar.JUNE: month = "-06-";  break;
                case Calendar.JULY: month = "-07-"; break;
                case Calendar.AUGUST: month = "-08-";  break;
                case Calendar.SEPTEMBER: month = "-09-"; break;
                case Calendar.OCTOBER: month = "-10-"; break;
                case Calendar.NOVEMBER: month = "-11-";  break;
                case Calendar.DECEMBER: month = "-12-";  break;
                default: month = "-NA-"; break;
            }
            sbuf.append(month);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if(day < 10)
                sbuf.append('0');
            sbuf.append(day);

            sbuf.append('T');

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if(hour < 10) {
                sbuf.append('0');
            }
            sbuf.append(hour);
            sbuf.append(':');

            int mins = calendar.get(Calendar.MINUTE);
            if(mins < 10) {
                sbuf.append('0');
            }
            sbuf.append(mins);
            sbuf.append(':');

            int secs = calendar.get(Calendar.SECOND);
            if(secs < 10) {
                sbuf.append('0');
            }
            sbuf.append(secs);

            // store the time string for next time to avoid recomputation
            sbuf.getChars(start, sbuf.length(), lastTimeString, 0);
            lastTime = now - millis;
        }
        else {
            sbuf.append(lastTimeString);
        }

        if(millis != 0) {
            sbuf.append('.');
            if (millis < 100)
                sbuf.append('0');
            if (millis < 10)
                sbuf.append('0');
            sbuf.append(millis);
        }

        int offset = calendar.get(Calendar.ZONE_OFFSET);
        if(offset == 0){
            sbuf.append('Z');
        }else{
            if(offset > 0)
                sbuf.append('+');
            else{
                sbuf.append('-');
                offset = Math.abs(offset);
            }

            int offsetMinutesTotal = offset / 1000 / 60;
            int hours = offsetMinutesTotal / 60;
            int minutes = offsetMinutesTotal % 60;

            sbuf.append(String.format(Locale.getDefault(), "%02d:%02d", hours, minutes));
        }
        return sbuf;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        try {
            return parse(source.substring(pos.getIndex()));
        }catch (ParseException e)
        {
            return null;
        }
    }

    @Override
    public Date parse(String source) throws ParseException {

        Matcher matcher = pattern.matcher(source);
        if(!matcher.matches()){
            return null;
        }

        String groupZ, group;
        groupZ = matcher.group(GRP_TZ_Z);//"tzz" 16
        group = matcher.group(GRP_TZ_SIGN);//"tzsign" 12
        if(groupZ != null && group != null) {
            throw new ParseException("Cannot parse TimeOffset! " + source, 0);
        }
        if(groupZ != null){
            calendar.set(Calendar.ZONE_OFFSET, 0);
        }else if(group != null){
            int multi = group.equals("+")? 1 : -1;
            int tzhour = getGroupInt(matcher,GRP_TZ_HOUR);//"tzhour" 13
            int tzminute = getGroupInt(matcher,GRP_TZ_MINUTE);//"tzminute" 15

            int offset = (tzhour*60 + tzminute) * 60 * 1000 * multi;
            calendar.set(Calendar.ZONE_OFFSET, offset);
        }else{
            calendar.set(Calendar.ZONE_OFFSET, 0);
        }

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, getGroupInt(matcher, GRP_YEAR));
        int groupInt = getGroupInt(matcher, GRP_MONTH);
        if(groupInt >= 1 && groupInt <= 12){
            calendar.set(Calendar.MONTH, groupInt-1);
        }else{
            throw new ParseException("Could not parse Month: " + source, 0);
        }

        calendar.set(Calendar.DAY_OF_MONTH, getGroupInt(matcher, GRP_DAY));

        if(matcher.group(GRP_HOUR_PRESENT) != null){
            calendar.set(Calendar.HOUR_OF_DAY, getGroupInt(matcher, GRP_HOUR));
            if(matcher.group(GRP_MINUTE_PRESENT) != null){
                calendar.set(Calendar.MINUTE, getGroupInt(matcher, GRP_MINUTE));
                if(matcher.group(GRP_SECOND_PRESENT) != null){
                    calendar.set(Calendar.SECOND, getGroupInt(matcher, GRP_SECOND));

                    group = matcher.group(GRP_MS);//"milis" 09
                    if(group != null && group.length() > 0){
                        if(group.length() > 6) {
                            throw new ParseException("Could not parse Miliseconds: " + source, 0);
                        }
                        int i = parseUnsignedInt(group);
                        if(i == -1) {
                            i = 0;
                        }
                        calendar.set(Calendar.MILLISECOND, i);
                    }else{
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                }
            }
        }

        return calendar.getTime();
    }

    private int getGroupInt(Matcher matcher, int group){
        String g = matcher.group(group);
        try {
            return Integer.parseInt(g);
        }catch (NumberFormatException ignored){}
        return 0;
    }

    private int parseUnsignedInt(String s){
        try {
            return Integer.parseInt(s);
        }catch (NumberFormatException ignored){}
        return -1;
    }
}