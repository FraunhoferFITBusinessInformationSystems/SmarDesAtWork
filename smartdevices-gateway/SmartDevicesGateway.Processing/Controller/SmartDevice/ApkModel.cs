//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class ApkModel : AbstractModel
    {
        private readonly IConfigService _configService;
        private string _resourceDirectory;

        public ApkModel(ILoggerFactory loggerFactory, IConfigService configService) : base(loggerFactory)
        {
            _configService = configService;
            SetResourceDirectory();
        }

        private void SetResourceDirectory()
        {
            var dirFromConfig = _configService.Get<AppConfig>().ResourceDirectory;
            var contentRoot = _configService.ContentRoot;
            _resourceDirectory = !Path.IsPathRooted(dirFromConfig) 
                ? Path.Combine(contentRoot, dirFromConfig) 
                : dirFromConfig;
        }

        public string GetApkDirectory()
        {
            if (_resourceDirectory == null)
            {
                throw new IOException("Unknown directory specified!");
            }

            var apkDir = Path.Combine(_resourceDirectory, "apk");
            if (!Directory.Exists(apkDir))
            {
                throw new IOException("Unknown directory specified!");
            }

            return apkDir;
        }

        public IEnumerable<ApkVersion> GetApkFiles()
        {
            var apkDir = GetApkDirectory();
            var files = Directory.GetFiles(apkDir, "*.apk");

            return files.Select(ParseApkFilename)
                .Where(x => x != null)
                .OrderBy(x => x, Comparer<ApkVersion>.Create((a, b) => b.CompareTo(a)))
                .ToList();
        }

        public ApkVersion GetApkPhoneLatest()
        {
            return GetApkFiles()
                .FirstOrDefault(x => x.PackageName.Equals(PhonePackageName));
        }
        public ApkVersion GetApkWatchLatest()
        {
            return GetApkFiles()
                .FirstOrDefault(x => x.PackageName.Equals(WatchPackageName));
        }

        private const string ApkFileNamePattern = @"([a-z._]*).(\d+).(\d+).(\d+)(-([a-z]*))?.apk";
        public const string PhonePackageName = "smartdevicesapp.app.phone";
        public const string WatchPackageName = "smartdevicesapp.app.watch";

        public class ApkVersion : IComparable<ApkVersion>
        {
            public string PackageName { get; set; }
            public string Version => new StringBuilder().Append(Major).Append('.').Append(Minor).Append('.').Append(Patch).ToString();
            public string ReleaseType { get; set; }

            public int Major { get; set; }

            public int Minor { get; set; }

            public int Patch { get; set; }


            [SuppressMessage("ReSharper", "EnforceIfStatementBraces")]
            [SuppressMessage("ReSharper", "ConvertIfStatementToReturnStatement")]
            public int CompareTo(ApkVersion other)
            {
                if (this == other) return 0;
                if (other == null) return -1;
                var diff = string.Compare(PackageName, other.PackageName, StringComparison.Ordinal);
                if (diff != 0) return diff;
                diff = Patch - other.Patch;
                if (diff != 0) return diff;
                diff = Major - other.Major;
                if (diff != 0) return diff;
                diff = Minor - other.Minor;
                if (diff != 0) return diff;
                return string.Compare(ReleaseType, other.ReleaseType, StringComparison.Ordinal);
            }

            public override string ToString()
            {
                return new StringBuilder(PackageName).Append('.')
                    .Append(Major).Append('.')
                    .Append(Minor).Append('.')
                    .Append(Patch).Append('-')
                    .Append(ReleaseType).Append(".apk")
                    .ToString();
            }
        }

        public static ApkVersion ParseApkFilename(string filename)
        {
            var ver = new ApkVersion();
            var match = Regex.Match(filename, ApkFileNamePattern);
            if (!match.Success)
            {
                return null;
            }
            var groups = match.Groups;
            if (groups[1].Success)
            {
                ver.PackageName = groups[1].Value;
            }
            if (groups[2].Success)
            {
                int.TryParse(groups[2].Value, out var i);
                ver.Major = i;
            }
            if (groups[3].Success)
            {
                int.TryParse(groups[3].Value, out var i);
                ver.Minor = i;
            }
            if (groups[4].Success)
            {
                int.TryParse(groups[4].Value, out var i);
                ver.Patch = i;
            }
            if (groups[6].Success)
            {
                ver.ReleaseType = groups[6].Value;
            }
            return ver;
        }
    }
}
