//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Globalization;

namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation
{
    public class DateTimeConverter
    {
        public const string dateTimeISO8601FormatString2 = ("yyyy-MM-ddTHH\\:mm\\:ss.fffffffzzz");
        public const string dateTimeISO8601FormatString = "yyyy-MM-dd'T'HH:mm:ss.FFFFFFFK";

        public static DateTime ToDateTime(string value)
        {
            value = value.Trim('"');
            var v = DateTime.ParseExact(value, dateTimeISO8601FormatString, CultureInfo.InvariantCulture);
            return v;
        }

        public static DateTimeOffset ToDateTimeOffset(string value)
        {
            value = value.Trim('"');
            var v = DateTimeOffset.ParseExact(value, dateTimeISO8601FormatString, CultureInfo.InvariantCulture);
            return v;
        }

        public static string ToString(DateTime dateTime)
        {
            var v = (dateTime).ToString(dateTimeISO8601FormatString);
            return v;
        }

        public static string ToString(DateTimeOffset dateTimeOffset)
        {
            var v = (dateTimeOffset).ToString(dateTimeISO8601FormatString);
            return v;
        }
    }
}
