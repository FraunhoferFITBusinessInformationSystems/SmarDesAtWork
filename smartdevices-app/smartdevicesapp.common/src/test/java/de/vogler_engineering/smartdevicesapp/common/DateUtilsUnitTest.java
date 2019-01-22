/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common;



import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.util.Pair;
import de.vogler_engineering.smartdevicesapp.common.test.categories.UnitTests;
import de.vogler_engineering.smartdevicesapp.common.util.DateUtils;
import de.vogler_engineering.smartdevicesapp.common.util.ISO8601DateFormat;

@Category(UnitTests.class)
public class DateUtilsUnitTest {

    @Test
    public void testIso8601DateFormatterSimple() throws ParseException {
        String[] testSet = {
                "2018-05-15T14:42:47",
                "2018-05-15T14:42:47+02:00",
                "2018-05-15T14:42:47.567Z",
                "2018-05-15T14:42:47.567+08:00",
                "2018-05-15T14:42:47Z",
                "1999-11-27T15:49:37.459Z",
                "1999-11-27T15:49:37.459+02:00",
                "1999-11-27T15:49:37.459-0200",
                "1999-11-27T15:49:37Z",
                "1999-11-27T15:49Z",
                "1999-11-27T15Z",
                "1999-11-27Z",
                "1999-11-27T15:49:37+02:00",
                "1999-11-27T15:49:37-0200"
        };

        DateFormat dateFormat = DateUtils.createJsonDateFormat();

        for (String dateString : testSet) {
            Date date = dateFormat.parse(dateString);
            Assert.assertNotNull(date);

            String str = dateFormat.format(date);
            Assert.assertNotNull(str);
            Assert.assertTrue(str.length() > 0);
        }
    }

    @Test
    public void testIso8601DateTrueNegative() {
        String[] testSet = {
//                "2018-05-15T14:42:47.",
//                "2018-05-15T14:42:47+2:0",
                "2018-05-15T14:42:47.567Z+01:00",
                "2018-05-15T14:42:47.567+08:00Z",
                "2018-05-15T14:42.32",
                "1999-11-27T15:49:37.4587598Z",
                "2018-10-05T12:Z",
                "2018-10-05TZ"
        };

        DateFormat dateFormat = new ISO8601DateFormat();
        for (String dateString : testSet) {
            try {
                Date date = dateFormat.parse(dateString);
                if (date == null)
                    throw new ParseException("Error in date!" + dateString, 0);
                Assert.fail("No Exception thrown! " + dateString);
            } catch (ParseException e) {
            }
        }
    }

    private ArrayList<Pair<String, Calendar>> getTestPairs(){
        ArrayList<Pair<String, Calendar>> pairs = new ArrayList<>();
        pairs.add(Pair.create("2016-05-24T09:58:32Z", createDate(2016, 5, 24, 9, 58, 32, 0, 0)));
        pairs.add(Pair.create("2017-04-24T09:58:00.123Z", createDate(2017, 4, 24, 9, 58, 0, 123)));
        pairs.add(Pair.create("2018-05-24T09:58:32.123+02:00", createDate(2018, 2, 28, 9, 58, 32, 123, 2)));
        pairs.add(Pair.create("2019-05-24T09:58:32+02:00", createDate(2019, 5, 24, 9, 58, 32, 0, 2)));
        pairs.add(Pair.create("0001-01-01T00:00:00", createDate(1, 1, 1, 0, 0, 0, 0)));
        return pairs;
    }

    //TODO fix this!
//    @Test
//    public void testIso8601DateFormatter() {
//        ArrayList<Pair<String, Calendar>> pairs = getTestPairs();
//
////        ISO8601DateFormat df = new ISO8601DateFormat(TimeZone.getTimeZone("UTC"));
//        ISO8601DateFormat df = new ISO8601DateFormat(TimeZone.getDefault());
//        SimpleDateFormat sdf = new SimpleDateFormat();
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        int i = 0;
//        for (Pair<String, Calendar> pair : pairs) {
//            //Check format
//            Date second = pair.second.getTime();
//            TimeZone timeZone = pair.second.getTimeZone();
//
//            System.out.println("Time zone: " + timeZone.getID());
//            System.out.println("Default time zone: " + TimeZone.getDefault().getID());
//            System.out.println("UTC:     " + sdf.format(pair.second.getTime()));
//            System.out.println("Default: " + pair.second.getTime());
//            System.out.println("ISO8601: " + df.format(pair.second.getTime()));
//
//            String format = df.format(second);
//            Assert.assertNotNull(format);
//            checkIsoString(format);
//            Assert.assertEquals("Idx"+i, pair.first, format);
//            //Check parsing
//
//            try {
//                Date date = df.parse(pair.first);
//                Assert.assertNotNull(date);
//                Assert.assertEquals("Idx"+i, date, pair.second.getTime());
//            } catch (ParseException e) {
//                Assert.fail(e.getMessage());
//            }
//            i++;
//        }
//    }

//    @Test
//    public void testIso8601DateParser() {
//        List<DatePair> list = new ArrayList<>();
//        ISO8601DateFormat df = new ISO8601DateFormat();
//
//        list.add(new DatePair("2018-05-24T09:58:32Z", createDate(2018, 5, 24, 9, 58, 32, 0, 0)));
//        list.add(new DatePair("2018-04-24T09:58:00.123Z", createDate(2018, 4, 24, 9, 58, 0, 123)));
//        list.add(new DatePair("2018-05-24T09:58:32.123+02:00", createDate(2018, 5, 24, 9, 58, 32, 123, 2)));
//        list.add(new DatePair("2018-05-24T09:58:32+02:00", createDate(2018, 5, 24, 9, 58, 32, 0, 2)));
//        list.add(new DatePair("0001-01-01T00:00:00", createDate(1, 1, 1, 0, 0, 0, 0)));
//
//        for (DatePair stringDateEntry : list) {
//            try {
//                Date parse = df.parse(stringDateEntry.string);
//                Assert.assertNotNull(parse);
//
//                Calendar c = GregorianCalendar.getInstance(df.getTimeZone());
//                c.setTime(parse);
//                Assert.assertEquals(0, stringDateEntry.date.compareTo(c));
//            } catch (ParseException e) {
//                Assert.fail();
//            }
//        }
//    }

    private void checkDate(DateFormat formatter, Date date, String string) throws ParseException {
        Date parse = formatter.parse(string);
        Assert.assertNotNull(parse);
        Assert.assertEquals(date.getTime(), parse.getTime());

        String format = formatter.format(date);
        checkIsoString(format);
        Assert.assertNotNull(format);
        Assert.assertEquals(string, format);
    }

    private Pattern iso8601Pattern;

    private void checkIsoString(String string) {
        if (iso8601Pattern == null) {
            iso8601Pattern = Pattern.compile("((\\d{4})-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d))(\\.(\\d+))?((([+-])(\\d\\d)(:?)(\\d\\d))|(Z))?");
        }
        Matcher matcher = iso8601Pattern.matcher(string);
        Assert.assertTrue("Incorrect String: "+string, matcher.matches());
    }

    private Calendar createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.ZONE_OFFSET, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private Calendar createDate(int year, int month, int day, int hour, int minute, int second, int milliseconds) {
        Calendar calendar = createDate(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        return calendar;
    }

    private Calendar createDate(int year, int month, int day, int hour, int minute, int second, int milliseconds, int timezoneHr) {
        Calendar calendar = createDate(year, month, day, hour, minute, second, milliseconds);
        calendar.set(Calendar.ZONE_OFFSET, timezoneHr * 60 * 60 * 1000);
        return calendar;
    }


}
