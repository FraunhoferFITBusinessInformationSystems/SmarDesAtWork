//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using LiteDB;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Persistence.Base;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types;
using SmartDevicesGateway.Model.Values;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Common.StandardLibraryExtensions;
using SmartDevicesGateway.Model.Resource;

namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation
{

    public class LiteDBWrapper : IDisposable
    {
        protected readonly LiteDatabase DB;
        public string DatabaseFileName { get; private set; }

        //collections
        protected Collections Collections { get; private set; } = new Collections();

        public LiteDBWrapper(Stream stream)
        {
            var mapper = InitMapper();
            DB = new LiteDatabase(stream, mapper);
            InitTables();
            InitBSONParser();
        }

        public LiteDBWrapper(string databaseFileName = "SmartDevicesGateway.db")
        {
            DatabaseFileName = databaseFileName;
            var mapper = InitMapper();
            DB = new LiteDatabase(DatabaseFileName, mapper);
            InitTables();
            InitBSONParser();
        }

        private void InitBSONParser()
        {
            BsonMapper.Global.RegisterType<DateTimeOffset>
              (
                  serialize: (dateTimeOffset) => new BsonValue(DateTimeConverter.ToString(dateTimeOffset)),
                  deserialize: (bson) => DateTimeConverter.ToDateTimeOffset(bson.ToString())
              );

            BsonMapper.Global.RegisterType<DateTime>
              (
                  serialize: (dateTime) => new BsonValue(DateTimeConverter.ToString(dateTime)),
                  deserialize: (bson) => DateTimeConverter.ToDateTime(bson.ToString())
              );

            BsonMapper.Global.ResolveMember += (type, memberInfo, member) =>
            {
                if (member.DataType == typeof(DateTime))
                {
                    member.Deserialize = (v, m) => DateTimeConverter.ToDateTime(v.ToString());
                    member.Serialize = (o, m) => new BsonValue(DateTimeConverter.ToString((DateTime)(o)));
                }
                if (member.DataType == typeof(DateTimeOffset))
                {
                    member.Deserialize = (v, m) => DateTimeConverter.ToDateTimeOffset(v.ToString());
                    member.Serialize = (o, m) => new BsonValue(DateTimeConverter.ToString((DateTimeOffset)(o)));
                }
            };
        }

        protected BsonMapper InitMapper()
        {
            var mapper = BsonMapper.Global;

            mapper.Entity<DeviceId>()
                .Id(x => x.FullId, false);

            mapper.Entity<DeviceInfo>()
                .Id(x => x.Id, false);

            mapper.Entity<Jobs.Job>()
                .Field(x => x.Id, "ID")
                .Id(x => x.LiteDbId, true);

            mapper.Entity<BufferedJob>()
                .Id(x => x.LiteDbId, true)
                .Field(x => x.Id, "ID")
                .DbRef(x => x.Job, TableNames.Job)
                .DbRef(x => x.DeviceId, TableNames.DeviceID);

            mapper.Entity<DeviceIdBufferedJobLink>()
                .Id(x => x.Id, true)
                .DbRef(x => x.DeviceId, TableNames.DeviceID)
                .DbRef(x => x.BufferedJobs, TableNames.BufferedJob);

            mapper.Entity<DeviceIdDeviceInfoLink>()
                .Id(x => x.Id, true)
                .DbRef(x => x.DeviceId, TableNames.DeviceID)
                .DbRef(x => x.DeviceInfo, TableNames.DeviceInfo);

            mapper.Entity<ResourceInfo>()
                .Field(x => x.Id, "ID")
                .Id(x => x.LiteDbId, true);

            return mapper;
        }

        protected void InitTables()
        {
            //deviceID
            Collections.DeviceID = DB.GetCollection<DeviceId>(TableNames.DeviceID);

            //deviceInfo
            Collections.DeviceInfo = DB.GetCollection<DeviceInfo>(TableNames.DeviceInfo);
            Collections.DeviceInfo.EnsureIndex(j => j.DeviceName);

            //job
            Collections.Job = DB.GetCollection<Jobs.Job>(TableNames.Job);
            Collections.Job.EnsureIndex(j => j.Name);

            //bufferedJob
            Collections.BufferedJob = DB.GetCollection<BufferedJob>(TableNames.BufferedJob);
         
            //deviceIdBufferedJobLink
            Collections.DeviceIdBufferedJobLink = DB.GetCollection<DeviceIdBufferedJobLink>(TableNames.DeviceIdBufferedJobLink);

            //deviceIdDeviceInfoLinkCollection
            Collections.DeviceIdDeviceInfoLink = DB.GetCollection<DeviceIdDeviceInfoLink>(TableNames.DeviceIdDeviceInfoLink);

            //resources
            Collections.ResourceInfo = DB.GetCollection<ResourceInfo>(TableNames.ResourceInfos);
        }

        //crud

        //--deviceId
        public void AddOrUpdate(DeviceId deviceId)
        {
            var foundDeviceIds = Collections.DeviceID.Find(x => x.FullId == deviceId.FullId);
            if (foundDeviceIds?.Any() == true)
            {
                //question: do we need update
                var foundDeviceId = foundDeviceIds.Single();
                foundDeviceId.DeviceName = deviceId.DeviceName;
                foundDeviceId.User = deviceId.User;
                Collections.DeviceID.Update(foundDeviceId);
            }
            else
            {
                Collections.DeviceID.Insert(deviceId);
            }
        }

        public IEnumerable<DeviceId> GetAllDeviceIds()
        {
            return Collections.DeviceID.FindAll();
        }

        //--deviceInfo
        public void AddOrUpdate(DeviceInfo deviceInfo)
        {
            var foundDeviceInfos = Collections.DeviceInfo.Find(x => x.Id == deviceInfo.Id);
            if (foundDeviceInfos?.Any() == true)
            {
                var foundDeviceInfo = foundDeviceInfos.Single();
                foundDeviceInfo.properties = deviceInfo.properties;
                foundDeviceInfo.DeviceName = deviceInfo.DeviceName;
                foundDeviceInfo.family = deviceInfo.family;
                foundDeviceInfo.FcmToken = deviceInfo.FcmToken;
                foundDeviceInfo.Type = deviceInfo.Type;
                Collections.DeviceInfo.Update(foundDeviceInfo);
            }
            else
            {
                var foundDeviceInfosToken = Collections.DeviceInfo.Find(x => x.FcmToken == deviceInfo.FcmToken).ToList();
                if (foundDeviceInfosToken.Any() == true)
                {
                    foreach (var info in foundDeviceInfosToken)
                    {
                        Collections.DeviceInfo.Delete(info.Id);
                    }
                }
                Collections.DeviceInfo.Insert(deviceInfo);
            }
        }

        public IEnumerable<DeviceInfo> GetAllDeviceInfos()
        {
            return Collections.DeviceInfo.FindAll();
        }

        //--deviceIdBufferedJobLink
        public void Remove(IEnumerable<DeviceIdBufferedJobLink> deviceIdBufferedJobLink)
        {
            var bufferedJobs = deviceIdBufferedJobLink.SelectMany(x => x.BufferedJobs).ToList();
            var jobs = bufferedJobs.Select(y => y.Job).ToList();

            Collections.Job.Delete(x => jobs.Select(j => j.Id).Contains(x.Id));
            Collections.BufferedJob.Delete(x => bufferedJobs.Select(b => b.Id).Contains(x.Id));
            Collections.DeviceIdBufferedJobLink.Delete(x => deviceIdBufferedJobLink.Select(d => d.Id).Contains(x.Id));
        }

        protected LiteCollection<DeviceIdBufferedJobLink> DeviceIdBufferedJobLinksBase => 
            Collections.DeviceIdBufferedJobLink
                .Include(x => x.BufferedJobs)
                .Include(x => x.BufferedJobs.Select(y => y.DeviceId))
                .Include(x => x.BufferedJobs.Select(y => y.Job))
                .Include(x => x.DeviceId);

        public IEnumerable<DeviceIdBufferedJobLink> GetDeviceIdBufferedJobLinks()
        {
            return DeviceIdBufferedJobLinksBase.FindAll();
        }

        public DeviceIdBufferedJobLink GetDeviceIdBufferedJobLink(DeviceId deviceId)
        {
            return DeviceIdBufferedJobLinksBase
                .Find(x => x.DeviceId.FullId == deviceId.FullId)
                .Single();
        }

        public DeviceIdBufferedJobLink GetDeviceIdBufferedJobLinkOrNull(DeviceId deviceId)
        {
            var found = DeviceIdBufferedJobLinksBase.Find(x => x.DeviceId.FullId == deviceId.FullId).ToList();

            if(found.Any())
            {
                return found.Single();
            }

            return null;
        }

        //--bufferedJob
        protected LiteCollection<BufferedJob> BufferedJobBase =>
            Collections.BufferedJob
                .Include(x => x.Job)
                .Include(x => x.DeviceId);

        public BufferedJob GetBufferedJob(Guid guid)
        {
            return BufferedJobBase
                .Find(x => x.Id == guid)
                .SingleOrDefault(y => y.Job.Id == guid);
        }

        //--deviceIdDeviceInfo
        protected LiteCollection<DeviceIdDeviceInfoLink> BaseDeviceIdDeviceInfoLink =>
            Collections.DeviceIdDeviceInfoLink
                .Include(x => x.DeviceId)
                .Include(x => x.DeviceInfo);

        //--job
        public IEnumerable<Jobs.Job> GetAllJobs()
        {
            return Collections.Job.FindAll();
        }

        //--resourceInfo
        public IEnumerable<ResourceInfo> GetResourceInfos()
        {
            return Collections.ResourceInfo.FindAll();
        }

        public ResourceInfo GetResourceInfo(string path)
        {
            return Collections.ResourceInfo.FindOne(x => x.RequestPath.Equals(path));
        }

        public void SetResourceInfo(ResourceInfo resourceInfo)
        {
            var oldValue = Collections.ResourceInfo.FindOne(x => x.RequestPath.Equals(resourceInfo.RequestPath));
            if (oldValue != null)
            {
                Collections.ResourceInfo.Delete(oldValue.LiteDbId);
            }
            Collections.ResourceInfo.Insert(resourceInfo);
        }

        public bool RemoveResourceInfo(string path)
        {
            return Collections.ResourceInfo.Delete(x => x.RequestPath.Equals(path)) > 0;
        }

        #region IDisposable Support
        private bool disposedValue = false; // Dient zur Erkennung redundanter Aufrufe.

        protected virtual void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                if (disposing)
                {
                    DB?.Dispose();
                }
                disposedValue = true;
            }
        }

        public void Dispose()
        {
            Dispose(true);
        }
        #endregion
    }
}
