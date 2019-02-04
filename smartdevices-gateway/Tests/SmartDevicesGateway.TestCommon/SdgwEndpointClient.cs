//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Threading.Tasks;
using System.Web;
using Microsoft.AspNetCore.Http;
using SmartDevicesGateway.Api.EndPoints;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Model.Dto;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Dto.Data;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Messages;
using SmartDevicesGateway.Model.Tabs;

namespace SmartDevicesGateway.TestCommon
{
    public class SdgwEndpointClient : JsonEndpointClient
    {

        public ApiResponse<string> GetEchoRequest()
        {
            const string uri = "/api/echo";
            return Get<string>(uri);
        }

        public ApiResponse<ServerInfo> GetConfigServerInfo()
        {
            const string uri = "/api/config/info/server";
            return Get<ServerInfo>(uri);
        }

        public ApiResponse<DeviceInfo> GetConfigDeviceInfo(DeviceId device)
        {
            var uri = $"/api/config/info/{device.FullId}";
            return Get<DeviceInfo>(uri);
        }

        public ApiResponse<DeviceInfo> PutConfigDeviceInfos(DeviceId device, DeviceInfo info)
        {
            var uri = $"/api/config/info/{device.FullId}";
            return Put<DeviceInfo>(uri, info);
        }

        public ApiResponse<ConfigDto> GetConfigDeviceConfig(DeviceId device)
        {
            var uri = $"/api/config/{device.FullId}";
            return Get<ConfigDto>(uri);
        }

        public ApiResponse<IEnumerable<TabConfig>> GetConfigDeviceTabs(DeviceId device)
        {
            var uri = $"/api/config/tabs/{device.FullId}";
            return GetAll<TabConfig>(uri);
        }

        public async Task<ApiResponse<IEnumerable<TabEntryDto>>> GetDataTab(DeviceId device, string tab,
            PaginationRequestDto paginationDto = null,
            SortRequestDto sortDto = null,
            AdvancedFilterRequestDto filterRequestDto = null)
        {
            
            var query = HttpUtility.ParseQueryString("");
            BuildQuery(query, paginationDto);
            BuildQuery(query, sortDto);
            BuildQuery(query, filterRequestDto);
            var uri = $"/api/data/tab/{device.FullId}/{tab}?{query.ToString()}";

            return await GetAllAsync<TabEntryDto>(uri);
        }

        public async Task<ApiResponse<IEnumerable<Job>>> GetDataJobs(DeviceId device, PaginationRequestDto pagination = null,
            FilterRequest filter = null,
            SortRequestDto sort = null)
        {
            var uri = $"/api/data/{device.FullId}/jobs";
            return await GetAllAsync<Job>(uri);
        }

        public ApiResponse<IEnumerable<ValueDto>> GetDataMessages(DeviceId device, PaginationRequestDto pagination = null,
            FilterRequest filter = null,
            SortRequestDto sort = null)
        {
            var uri = $"/api/data/{device.FullId}/values";
            return GetAll<ValueDto>(uri);
        }

        public ApiResponse<IEnumerable<Message>> GetDataMessage(DeviceId device, PaginationRequestDto pagination = null,
            FilterRequest filter = null,
            SortRequestDto sort = null)
        {
            var uri = $"/api/data/{device.FullId}/messages";
            return GetAll<Message>(uri);
        }

        public ApiResponse<IEnumerable<Message>> GetData(DeviceId device, DataDtoTypes? entity = null, PaginationRequestDto pagination = null,
            FilterRequest filter = null,
            SortRequestDto sort = null)
        {
            var uri = $"/api/data/{device.FullId}";
            return GetAll<Message>(uri);
        }

        public ApiResponse<object> GetSendFirebaseTest(DeviceId device, string msg, bool notify = true)
        {
            var uri = $"/api/test/sendfirebase/{msg}/{device.FullId}?notify={notify.ToString()}";
            return Get<object>(uri);
        }

        public ApiResponse<ApkOverviewDto> GetApkInfo()
        {
            var uri = $"/api/apk";
            return Get<ApkOverviewDto>(uri);
        }

        public ApiResponse<FileStream> GetApkPhoneLatest()
        {
            var uri = $"/api/apk/phone";
            return null;
        }

        public ApiResponse<FileStream> GetApkWatchLatest()
        {
            var uri = $"/api/apk/watch";
            return null;
        }

        public ApiResponse<FileStream> GetApkSpecific(string name)
        {
            var uri = $"/api/apk/{name}";
            return null;
        }

        public async Task<ApiResponse<Job>> PutActionsJobAsync(DeviceId device, Job job)
        {
            var uri = $"/api/actions/jobs/{device.FullId}";
            return await PutAsync<Job>(uri, job);
        }

        public ApiResponse<object> GetActionsClearDeviceJobs(DeviceId device)
        {
            var uri = $"/api/actions/jobs/{device.FullId}/clearjobs";
            return Get<object>(uri);
        }

        public ApiResponse<object> DeleteActionsClearAllJobs(DeviceId device)
        {
            var uri = $"/api/actions/jobs/clearalljobs";
            return Delete<object>(uri);
        }

        public async Task<ApiResponse<MessageReply>> PutActionsMessageAsync(DeviceId device, MessageReply reply)
        {
            var uri = $"/api/actions/messages/{device.FullId}";
            return await PutAsync(uri, reply);
        }

        public ApiResponse<FileStream> GetMediaImage(Guid id)
        {
            var uri = $"/api/media/images/{id.ToString()}";
            return null;
        }

        public void BuildQuery(NameValueCollection query, SortRequestDto sortRequest)
        {
            if (sortRequest == null)
            {
                return;
            }
            query["SortBy"] = sortRequest.SortBy;
            query["SortOrderAscending"] = sortRequest.SortOrderAscending.ToString();
        }

        public void BuildQuery(NameValueCollection query, PaginationRequestDto paginationRequest)
        {
            if (paginationRequest == null)
            {
                return;
            }
            query["PageNumber"] = paginationRequest.PageNumber.ToString();
            query["PageSize"] = paginationRequest.PageSize.ToString();
        }

        public void BuildQuery(NameValueCollection query, AdvancedFilterRequestDto filterRequest)
        {
            if (filterRequest == null)
            {
                return;
            }
            for (var i = 0; i < filterRequest.FilterBy.Length; i++)
            {
                query[$"FilterBy[{i}]"] = filterRequest.FilterBy[i];
                query[$"FilterExcluding[{i}]"] = filterRequest.FilterExcluding[i].ToString();
                query[$"FilterString[{i}]"] = filterRequest.FilterString[i];
            }
        }

        

        //        public ApiResponse<MediaEndPoint.FileUploadResponse> PostMediaImage(MediaEndPoint.FileUploadRequest file)
        //        {
        //            var uri = $"/api/media/images";
        //            return null;
        //        }
    }
}
