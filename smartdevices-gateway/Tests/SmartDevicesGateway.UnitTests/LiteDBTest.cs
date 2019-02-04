//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using SmartDevicesGateway.UnitTests.Fixtures;
using SmartDevicesGateway.UnitTests.Helper;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.CompilerServices;
using SmartDevicesGateway.TestCommon;
using Xunit;

namespace SmartDevicesGateway.UnitTests
{
    [Trait("Category", Categories.UNIT)]
    public class LiteDBTest : LiteDBTestBase, IClassFixture<LoggingFixture> //todo: generate teardown to delete test db's
    {
        public LoggingFixture Fixture { get; }
        private readonly ILogger _logger;

        public LiteDBTest(LoggingFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<LiteDBTest>();
        }

        //some basic tests and understanding of liteDB
        [Fact]
        public void ConfigureTest()
        {
            var liteDBPersistenceProvider = new LiteDBPersistenceProvider();
        }

        [Fact]
        public void SavingDeviceIDTest()
        {
            LiteDBBaseMock liteDBBaseMock = DeleteAndInitTestDB();

            var deviceId1 = new DeviceId("user", "deviceName");
            var deviceId2 = new DeviceId("user", "deviceName2");

            liteDBBaseMock.SaveDeviceId(deviceId1);

            var devicesFromDB = liteDBBaseMock.LoadDeviceId("user", "deviceName");

            Assert.Single(devicesFromDB);
            MyAsserts.Equal(deviceId1, devicesFromDB.First());
            Assert.Equal(deviceId1.FullId, devicesFromDB.First().FullId);
        }

        [Fact]
        public void SavingDeviceIDTest2()
        {
            LiteDBBaseMock liteDBBaseMock = DeleteAndInitTestDB();

            var deviceId1 = new DeviceId("user", "deviceName");
            var deviceId2 = new DeviceId("user", "deviceName");

            liteDBBaseMock.SaveDeviceId(deviceId1);
            Assert.Throws<LiteDB.LiteException>(() => liteDBBaseMock.SaveDeviceId(deviceId2));
        }

        [Fact]
        public void SavingJobBufferTest()
        {
            LiteDBBaseMock liteDBBaseMock = DeleteAndInitTestDB();

            var bufferedJobs = liteDBBaseMock.LoadAllBufferedJobs();

            var testDateTime = DateTimeOffset.Now;
            var testGuid = new Guid(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
            
            BufferedJob bufferedJob = null;

            bufferedJob = new BufferedJob()
            {
                CreateDate = testDateTime,
                DeviceId = new DeviceId("user.deviceName"),
                Id = testGuid,
                Job = new Job()
                {
                    AssignedAt = testDateTime,
                    CreatedAt = testDateTime,
                    AssignedTo = "trainee",
                    CreatedBy = "boss",
                    Id = testGuid,
                    Immediate = true,
                    Name = "jobkey",
                    Priority = Model.Messages.Enums.MessagePriority.Normal,
                    ReferenceId = testGuid,
                    Status = Model.Jobs.Enums.JobStatus.InProgress,
                    Type = "type",
                    Resource = new Dictionary<string, string>()
                    {
                        { "resourceKey1", "resourceValue1"},
                        { "resourceKey2", "resourceValue2"},
                        { "resourceKey3", "resourceValue3"}
                    }
                },
                ReplyDate = testDateTime,
                Sent = true,
                SentDate = testDateTime,
                Header = new System.Collections.Generic.Dictionary<string, object>()
                {
                    { "headerKey1", new ComplexObject() }
                }
            };

            liteDBBaseMock.SaveBufferedJob(bufferedJob);

            var bufferedJobFromDB = liteDBBaseMock.LoadAllBufferedJobs().ToList();

            Assert.Single(bufferedJobFromDB);
            MyAsserts.Equal(bufferedJob, bufferedJobFromDB.First());
        }

        //tests for the implementation of IPersistenceProvider
        [Fact]
        public void AddJobTest()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var foundJobs = liteDBPersistenceProvider.GetCollections().Job.FindAll().ToList();
            var foundBufferedJobs = liteDBPersistenceProvider.GetCollections().BufferedJob.Include(x => x.Job).FindAll().ToList();

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

        }

        [Fact]
        public void AddJobTest2()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            //1st element
            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //2nd element for 1st link
            var job2 = TestDataGenerator.GetJob(2);
            liteDBPersistenceProvider.AddJob(deviceId, job2);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(1, count);

            var allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(2, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job2, found.BufferedJobs[1].Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //1st element for 2nd link
            var deviceId2 = TestDataGenerator.GetDeviceId(2);
            var job3 = TestDataGenerator.GetJob(3);

            liteDBPersistenceProvider.AddJob(deviceId2, job3);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(2, count);

            allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(3, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId2);

            MyAsserts.Equal(job3, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId2, found.DeviceId);
        }

        [Fact]
        public void RemoveJobTest()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            //1st element
            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //2nd element for 1st link
            var job2 = TestDataGenerator.GetJob(2);
            liteDBPersistenceProvider.AddJob(deviceId, job2);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(1, count);

            var allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(2, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job2, found.BufferedJobs[1].Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //1st element for 2nd link
            var deviceId2 = TestDataGenerator.GetDeviceId(2);
            var job3 = TestDataGenerator.GetJob(3);

            liteDBPersistenceProvider.AddJob(deviceId2, job3);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(2, count);

            allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(3, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId2);

            MyAsserts.Equal(job3, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId2, found.DeviceId);

            //remove elements
            liteDBPersistenceProvider.RemoveJob(deviceId);
            var foundElements2 = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinkOrNull(deviceId);
            Assert.Null(foundElements2);

            Assert.Single(liteDBPersistenceProvider.GetCollections().Job.FindAll());
            Assert.Single(liteDBPersistenceProvider.GetCollections().BufferedJob.FindAll());
            Assert.Single(liteDBPersistenceProvider.GetCollections().DeviceIdBufferedJobLink.FindAll());
        }

        [Fact]
        public void RemoveAllJobsTest()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            //1st element
            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //2nd element for 1st link
            var job2 = TestDataGenerator.GetJob(2);
            liteDBPersistenceProvider.AddJob(deviceId, job2);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(1, count);

            var allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(2, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job2, found.BufferedJobs[1].Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //1st element for 2nd link
            var deviceId2 = TestDataGenerator.GetDeviceId(2);
            var job3 = TestDataGenerator.GetJob(3);

            liteDBPersistenceProvider.AddJob(deviceId2, job3);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(2, count);

            allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(3, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId2);

            MyAsserts.Equal(job3, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId2, found.DeviceId);

            //remove elements
            liteDBPersistenceProvider.RemoveAllJobs();
           
            Assert.Empty(liteDBPersistenceProvider.GetCollections().Job.FindAll());
            Assert.Empty(liteDBPersistenceProvider.GetCollections().BufferedJob.FindAll());
            Assert.Empty(liteDBPersistenceProvider.GetCollections().DeviceIdBufferedJobLink.FindAll());
        }

        [Fact]
        public void FindJobByIdTest()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            //1st element
            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //2nd element for 1st link
            var job2 = TestDataGenerator.GetJob(2);
            liteDBPersistenceProvider.AddJob(deviceId, job2);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(1, count);

            var allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(2, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job2, found.BufferedJobs[1].Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //1st element for 2nd link
            var deviceId2 = TestDataGenerator.GetDeviceId(2);
            var job3 = TestDataGenerator.GetJob(3);

            liteDBPersistenceProvider.AddJob(deviceId2, job3);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(2, count);

            allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(3, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId2);

            MyAsserts.Equal(job3, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId2, found.DeviceId);

            //find object
            var testObject = liteDBPersistenceProvider.FindJobById(job3.Id);
            MyAsserts.Equal(job3, testObject.Job);
            MyAsserts.Equal(deviceId2, testObject.DeviceId);
        }

        [Fact]
        public void TryGetDeviceInfosTest()
        {
            var ldbpp = DeleteAndInitTestDB2();

            var deviceId1 = TestDataGenerator.GetDeviceId(0);
            var deviceId2 = TestDataGenerator.GetDeviceId(1);

            var deviceInfo1 = TestDataGenerator.GetDeviceInfo(deviceId1.User, 0);
            deviceInfo1.FcmToken = "fcmtoken1";
            var deviceInfo2 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 1);
            deviceInfo1.FcmToken = "fcmtoken2";
            var deviceInfo3 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 2);
            deviceInfo1.FcmToken = "fcmtoken3";

            ldbpp.AddOrUpdateDeviceInfos(deviceId1, deviceInfo1);

            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo2);
            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo3);

            Assert.Equal(2, ldbpp.DeviceIdDeviceInfoLink.FindAll().Count());

            //deviceLink1
            var deviceLinks1 = ldbpp.DeviceIdDeviceInfoLink.Find(x => x.DeviceId.FullId == deviceId1.FullId);
            Assert.Single(deviceLinks1);
            var deviceLink1 = deviceLinks1.Single();

            MyAsserts.Equal(deviceId1, deviceLink1.DeviceId);
            MyAsserts.Equal(deviceInfo1, deviceLink1.DeviceInfo);

            //deviceLink2
            var deviceLinks2 = ldbpp.DeviceIdDeviceInfoLink.Find(x => x.DeviceId.FullId == deviceId2.FullId);
            Assert.Single(deviceLinks2);
            var deviceLink2 = deviceLinks2.Single();

            MyAsserts.Equal(deviceId2, deviceLink2.DeviceId);
            MyAsserts.Equal(deviceInfo3, deviceLink2.DeviceInfo);  
        }

        //helper functions to set up tests
        protected LiteDBBaseMock DeleteAndInitTestDB([CallerMemberName]string testName = "")
        {
            DBName = $"testdb_{testName}.db";            
            DeleteTestDB(DBName);
            var liteDBBaseMock = new LiteDBBaseMock(DBName);
            LiteDBWrapper = liteDBBaseMock;
            return liteDBBaseMock;
        }        

        protected LiteDBPersistenceProviderMock DeleteAndInitTestDB2([CallerMemberName]string testName = "")
        {
            DBName = $"testdb_{testName}.db";
            DeleteTestDB(DBName);
            var liteDBBaseMock = new LiteDBPersistenceProviderMock(DBName);
            LiteDBWrapper = liteDBBaseMock;
            return liteDBBaseMock;
        }

        [Fact]
        public void GetAllJobsTest()
        {
            var liteDBPersistenceProvider = DeleteAndInitTestDB2();

            //1st element
            var deviceId = TestDataGenerator.GetDeviceId();
            var job = TestDataGenerator.GetJob();

            liteDBPersistenceProvider.AddJob(deviceId, job);

            var allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            var count = allFound.Count();
            Assert.Equal(1, count);

            var found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job, found.BufferedJobs.Single().Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //2nd element for 1st link
            var job2 = TestDataGenerator.GetJob(2);
            liteDBPersistenceProvider.AddJob(deviceId, job2);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(1, count);

            var allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(2, allFoundBufferedJobs.Count());

            found = liteDBPersistenceProvider.GetDeviceIdBufferedJobLink(deviceId);

            MyAsserts.Equal(job2, found.BufferedJobs[1].Job);
            MyAsserts.Equal(deviceId, found.DeviceId);

            //1st element for 2nd link
            var deviceId2 = TestDataGenerator.GetDeviceId(2);
            var job3 = TestDataGenerator.GetJob(3);

            liteDBPersistenceProvider.AddJob(deviceId2, job3);

            allFound = liteDBPersistenceProvider.GetDeviceIdBufferedJobLinks().ToList();
            count = allFound.Count();
            Assert.Equal(2, count);

            allFoundBufferedJobs = allFound.SelectMany(x => x.BufferedJobs);
            Assert.Equal(3, allFoundBufferedJobs.Count());

            var found5 = liteDBPersistenceProvider.GetAllJobs();

            Assert.Equal(3, found5.Count());
        }

        [Fact]
        public void  GetAllDeviceIdsTest()
        {
            var ldbpp = DeleteAndInitTestDB2();

            var deviceId1 = TestDataGenerator.GetDeviceId(0);
            var deviceId2 = TestDataGenerator.GetDeviceId(1);

            var deviceInfo1 = TestDataGenerator.GetDeviceInfo(deviceId1.User, 0);
            var deviceInfo2 = TestDataGenerator.GetDeviceInfo(deviceId1.User, 1);
            var deviceInfo3 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 2);
            var deviceInfo4 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 3);
            var deviceInfo5 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 4);

            ldbpp.AddOrUpdateDeviceInfos(deviceId1, deviceInfo1);
            ldbpp.AddOrUpdateDeviceInfos(deviceId1, deviceInfo2);

            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo3);
            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo4);
            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo5);
            
            var objectsFromDB = ldbpp.GetAllDeviceIds().ToList();
            Assert.Equal(2, objectsFromDB.Count());
            MyAsserts.Equal(objectsFromDB[0], deviceId1);
            MyAsserts.Equal(objectsFromDB[1], deviceId2);
        }

        [Fact]
        public void  GetAllDeviceInfosTest()
        {
            var ldbpp = DeleteAndInitTestDB2();

            var deviceId1 = TestDataGenerator.GetDeviceId(0);
            var deviceId2 = TestDataGenerator.GetDeviceId(1);

            var deviceInfo1 = TestDataGenerator.GetDeviceInfo(deviceId1.User, 0);
            deviceInfo1.FcmToken = "fcmtoken1";
            var deviceInfo2 = TestDataGenerator.GetDeviceInfo(deviceId1.User, 1);
            deviceInfo2.FcmToken = "fcmtoken2";
            var deviceInfo3 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 2);
            deviceInfo3.FcmToken = "fcmtoken3";
            var deviceInfo4 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 3);
            deviceInfo4.FcmToken = "fcmtoken4";
            var deviceInfo5 = TestDataGenerator.GetDeviceInfo(deviceId2.User, 4);
            deviceInfo5.FcmToken = "fcmtoken5";

            ldbpp.AddOrUpdateDeviceInfos(deviceId1, deviceInfo1);
            ldbpp.AddOrUpdateDeviceInfos(deviceId1, deviceInfo2);

            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo3);
            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo4);
            ldbpp.AddOrUpdateDeviceInfos(deviceId2, deviceInfo5);
            
            var objectsFromDB = ldbpp.GetAllDeviceInfos().OrderBy(x => x.DeviceName).ToList();
            Assert.Equal(2, objectsFromDB.Count());
            MyAsserts.Equal(objectsFromDB[0], deviceInfo2);
            MyAsserts.Equal(objectsFromDB[1], deviceInfo5);
        }

        //[Fact]
        //public void SpecificTest1()
        //{
        //    var ldbpp = DeleteAndInitTestDB2();

        //    var deviceId = new DeviceId
        //    {
        //        DeviceName = "device1",
        //        User = "user1"
        //    };

        //    var deviceInfo = new DeviceInfo
        //    {
        //        AdditionalProperties = new Dictionary<string, object>()
        //        {
        //            {"Brand", (object)("Android")},
        //            {"Model", (object)("Android SDK built for x86")},
        //            {"Dimensions", (object)("Normal")},
        //            {"ReleaseVersion", (object)(6.0f)},
        //            {"Manufacturer", (object)("Unknown")},
        //            {"BuildId", (object)("sdk_google_phone_x86-userdebug 6.0 MASTER 4729342 test-keys")},
        //            {"ApiLevel", (object)(23)},
        //        },
        //        DeviceName = "device1",
        //        Family = Model.Enums.DeviceFamily.Watch,
        //        FcmToken = "token1",
        //        Id = "user1.device1",
        //        Type = Model.Enums.DeviceType.Android
        //    };

        //    ldbpp.AddOrUpdateDeviceInfos(deviceId, deviceInfo);
        //    var x = ldbpp.GetAllDeviceIds().Single();
        //    var y = ldbpp.GetAllDeviceInfos().Single();

        //    //fix me
        //}

        //[Fact]
        //public void SpecificTest2()
        //{
        //    var ldbpp = DeleteAndInitTestDB2();

        //    var family = new JObject();
        //    family.Add("Family", new JValue((object)null));

        //    var properties = new JObject();
        //    properties.Add("Brand", new JValue("Android"));
        //    properties.Add("Model", new JValue("Android SDK built for x86"));
        //    properties.Add("Dimensions", new JValue("Normal"));
        //    properties.Add("ReleaseVersion", new JValue(6.0f));
        //    properties.Add("Manufacturer", new JValue("Unknown"));
        //    properties.Add("BuildId", new JValue("sdk_google_phone_x86-userdebug 6.0 MASTER 4729342 test-keys"));
        //    properties.Add("ApiLevel", new JValue(23));

        //    var deviceInfo = new DeviceInfo
        //    {   
        //        AdditionalProperties = new Dictionary<string, object>()
        //        {
        //            {"Family", family},
        //            {"properties", properties},
        //        },
        //        DeviceName = "device1",
        //        Family = Model.Enums.DeviceFamily.Watch,
        //        FcmToken = "token1",
        //        Id = "user1.device1",
        //        Type = Model.Enums.DeviceType.Android
        //    };

        //    var deviceId = new DeviceId
        //    {
        //        DeviceName = "device1",
        //        User = "user1"
        //    };

        //    ldbpp.AddOrUpdateDeviceInfos(deviceId, deviceInfo);
        //    var x = ldbpp.GetAllDeviceIds().Single();
        //    var y = ldbpp.GetAllDeviceInfos().Single();
        //}
        [Fact]
        public void AddJobTestSimple()
        {
            var liteDbMock = DeleteAndInitTestDB();

            var job = TestDataGenerator.GetJob(1);

            liteDbMock.Collections.Job.Insert(job);

            var foundJob =liteDbMock.Collections.Job.FindAll().Single();

            MyAsserts.Equal(job, foundJob);
        }

        [Fact]
        public void UpdateJobStatus()
        {
            var ldbpp = DeleteAndInitTestDB2();

            var deviceId1 = TestDataGenerator.GetDeviceId(1);
            var job1 = TestDataGenerator.GetJob(1);

            ldbpp.AddJob(deviceId1, job1);

            var foundJob1unmod = ldbpp.GetCollections().Job.FindAll().Single();
            MyAsserts.Equal(job1, foundJob1unmod);

            ldbpp.UpdateJobStatus(job1.Id, Model.Jobs.Enums.JobStatus.Done);

            var foundJob1mod = ldbpp.GetCollections().Job.FindAll().Single();
            job1.Status = Model.Jobs.Enums.JobStatus.Done;
            MyAsserts.Equal(job1, foundJob1mod);

            var foundBufferedJob = ldbpp.GetBufferedJob(job1.Id);
            MyAsserts.Equal(job1, foundBufferedJob.Job);
        }
        [Fact]
        public void UpdateJobStatus2()
        {
            //this simulates current problem with duplicate ids from rules engine
            var ldbpp = DeleteAndInitTestDB2();

            var deviceId1 = TestDataGenerator.GetDeviceId(1);
            var job1 = TestDataGenerator.GetJob(1);
            var job2 = TestDataGenerator.GetJob(2);
            job2.Id = job1.Id; //<-- //this simulates current problem with duplicate ids from rules engine
            job2.CreatedBy = "creator2";

            ldbpp.AddJob(deviceId1, job1);
            ldbpp.AddJob(deviceId1, job2);

            var foundJob1unmod = ldbpp.GetCollections().Job.FindAll().ToList()[0];
            var foundJob2unmod = ldbpp.GetCollections().Job.FindAll().ToList()[1];
            MyAsserts.Equal(job1, foundJob1unmod);
            MyAsserts.Equal(job2, foundJob2unmod);

            ldbpp.UpdateJobStatus(job1.Id, Model.Jobs.Enums.JobStatus.Done);

            var foundJob1mod = ldbpp.GetCollections().Job.FindAll().ToList()[0];
            var foundJob2mod = ldbpp.GetCollections().Job.FindAll().ToList()[1];
            job1.Status = Model.Jobs.Enums.JobStatus.Done;
            job2.Status = Model.Jobs.Enums.JobStatus.Done;
            MyAsserts.Equal(job1, foundJob1mod);
            MyAsserts.Equal(job2, foundJob2mod);

            Assert.Throws<InvalidOperationException>(() => ldbpp.GetBufferedJob(job1.Id));
        }
    }
}
