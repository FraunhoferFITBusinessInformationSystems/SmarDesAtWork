/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * Created by vh on 28.02.2018.
 */

public class DateUtils {

    private static final String TAG = "DateUtils";

    private static DateFormat jsonDateFormat;
    private static DateFormat viewDateFormat;

    //Problem: "0001-01-01T00:00:00"

    public static DateFormat getJsonDateFormat() {
        if (jsonDateFormat == null)
            //jsonDateFormat = new JsonDateFormat();
            jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        return jsonDateFormat;
    }

    public static DateFormat createJsonDateFormat() {
        //return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        return new ISO8601DateFormat();
    }

    public static DateFormat createHumanReadableDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return dateFormat;
    }
//    public static String formatJsonDate(Date date) {
//        return getJsonDateFormat().format(date);
//    }

//    public static Date parseJsonDate(String dateStr) {
//        try {
//            try {
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
//                return df.parse(dateStr);
//            } catch (IllegalArgumentException ex) {
//                // error happen in Java 6: Unknown pattern character 'X'
//                if (dateStr.endsWith("Z")) dateStr = dateStr.replace("Z", "+0000");
//                else dateStr = dateStr.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
//                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
//                return df1.parse(dateStr);
//            }
//        } catch (ParseException e) {
//            Timber.tag(TAG).e("Could not parse json-date-string");
//            return null;
//        }
//    }

    //https://stackoverflow.com/a/43997852/3607486
    //    public static DateFormat getJsonDateFormat(){
//        if(jsonDateFormat == null)
//            jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
//        return jsonDateFormat;
//        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
////        @SuppressLint("SimpleDateFormat")
////        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
//    }


    public static DateFormat getViewDateFormat() {
        if (viewDateFormat == null)
            viewDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        return viewDateFormat;
    }

    private static class JsonDateFormat extends DateFormat {

        private final DateFormat format1, format2, format3;

        public JsonDateFormat(){ //"X" is not available at API 23 - available at 24+
            format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
            format3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        }

        protected JsonDateFormat(DateFormat format1, DateFormat format2, DateFormat format3){
            this.format1 = format1;
            this.format2 = format2;
            this.format3 = format3;
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return format1.format(date, toAppendTo, fieldPosition);
        }

        @Override
        public Date parse(String source, ParsePosition pos) {
            try {
                try {
                    Timber.tag(TAG).d("Parsing Date 1");
                    Date d = format1.parse(source);
                    if(d == null) throw new ParseException(source, pos.getIndex());
                    return d;
                } catch (IllegalArgumentException | ParseException ex) {
                    // error happen in Java 6: Unknown pattern character 'X'
                    Timber.tag(TAG).d("Parsing Date 2");
                    if (source.endsWith("Z")) source = source.replace("Z", "+0000");
                    else source = source.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
                    return format2.parse(source);
                }
            } catch (ParseException e) {
                try{
                    Timber.tag(TAG).d("Parsing Date 3");
                    return format3.parse(source);
                } catch (ParseException e1) {
                    Timber.tag(TAG).d("Parsing Date 4");
                    return null;
                }

            }
        }

        @Override
        public Object clone() {
            return new JsonDateFormat(format1, format2, format3);
        }
    }

//    public static class Iso8601DateFormat extends SimpleDateFormat {
//
//        private final static String regex = "(\\d{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)(\\.\\d+)?(([+-](\\d\\d):(\\d\\d))|(Z))?";
//        private final Pattern pattern;
//
//        public Iso8601DateFormat() {
//            super("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
//            this.pattern = Pattern.compile(regex);
//        }
//
//        @Override
//        public StringBuffer format(@NonNull Date date, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
//            DateTime.time
//
//
//            StringBuffer sb = super.format(date, toAppendTo, pos);
//
//            int n = (int) (date.getTime() % 1000L);
//            int milis = n<0 ? n+1000 : n;
//            if(milis != 0){
//                int miliOffset = (int) (1000f / milis);
//                sb.append(".");
//                sb.append(milis);
//            }
//
//
//
//            return sb;
//        }
//
//        @Override
//        public Date parse(@NonNull String source, @NonNull ParsePosition pos) {
//            Matcher matcher = pattern.matcher(source);
//
//
//
//            if(source.length() == 19){ //"0001-01-01T00:00:00".length()
//                source = source + "+0000";
//            }
//
//            if (source.endsWith("Z")) source = source.replace("Z", "+0000");
//            else source = source.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
//
//            return super.parse(source, pos);
//        }
//    }


//      public static class Iso8601DateFormat extends SimpleDateFormat {
//
//        private final static String regex = "(\\d{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)(\\.\\d+)?(([+-](\\d\\d)(:?)(\\d\\d))|(Z))?";
//        private final Pattern pattern;
//
//        public Iso8601DateFormat() {
//            super("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
//            this.pattern = Pattern.compile(regex);
//        }
//
//        @Override
//        public Date parse(@NonNull String source, @NonNull ParsePosition pos) {
//            Matcher matcher = pattern.matcher(source);
//            if(!matcher.matches()){
//                return null;
//            }
//
//            StringBuffer buffer = new StringBuffer();
//            buffer.append(matcher.group(1));
//            buffer.append()
//
//
//
//            String mili = matcher.group(2);
//
//
//
////            if(source.contains(".") || source.contains(",")){ //Remove millisecond part
////
////            }
//
//            if(source.length() == 19){ //"0001-01-01T00:00:00".length()
//                source = source + "+0000";
//            }
//
//            if (source.endsWith("Z")) source = source.replace("Z", "+0000");
//            else source = source.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
//
//            return super.parse(source, pos);
//        }
//    }
}
