//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Linq;
using System.Text;

// ReSharper disable once CheckNamespace
namespace System
{
    public static class StringExtensions
    {
        public static string Capitalize(this string s)
        {
            if (string.IsNullOrEmpty(s))
            {
                return string.Empty;
            }

            var a = s.ToCharArray();
            a[0] = char.ToUpper(a[0]);

            return new string(a);
        }


        public static string RemoveWhiteSpace(this string str)
        {
            var sb = new StringBuilder(str.Length);
            foreach (var c in str.Where(c => !char.IsWhiteSpace(c)))
            {
                sb.Append(c);
            }

            return sb.ToString();
        }


        public static bool Contains(this string source, string toCheck, StringComparison comp)
        {
            return source != null && toCheck != null && source.IndexOf(toCheck, comp) >= 0;
        }
    }
}
