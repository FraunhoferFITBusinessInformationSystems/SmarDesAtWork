//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Common
{
    public static class FileUtils
    {
        public static string GetFileEnding(string filename)
        {
            var i = filename.LastIndexOf(".", StringComparison.Ordinal);
            if (i != -1 && i < (filename.Length - 1))
            {
                return filename.Substring(i + 1);
            }
            return null;
        }

        public static string GetContentTypeFromEnding(string ending)
        {
            switch (ending)
            {
                case "png":
                    return "image/png";
                case "jpg":
                    return "image/jpeg";
                case "json":
                    return "application/json";
                case "txt":
                    return "text/plain";
                case "xml":
                    return "text/xml";
                case "apk":
                    return "application/vnd.android.package-archive";
                default:
                    return "application/octet-stream";
            }
        }

        public static string GetEndingFromContentType(string contentType)
        {
            switch (contentType)
            {
                case "image/png":
                    return "png";
                case "image/jpeg":
                    return "jpg";
                case "application/json":
                    return "json";
                case "text/plain":
                    return "txt";
                case "text/xml":
                    return "xml";
                case "application/vnd.android.package-archive":
                    return "apk";
                default:
                    return "obj";
            }
        }

    }
}
