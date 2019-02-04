//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Net;
using System.Text;
using Xunit;

namespace SmartDevicesGateway.TestCommon
{
    public class JsonEndpointTestClient : JsonEndpointClient
    {
        /// <summary>
        /// Dictionary of Types to Endpoints.
        /// Endpoints are defined through a String after the RootUrl. Each Endpoint
        /// delegated to a specific type, to wich the JsonObject ist parsed to/from.
        /// </summary>
        //        public readonly Dictionary<Type, string> EndPoints = new Dictionary<Type, string>();
        //        {
        ////            { typeof(RohstoffDto), "/api/stammdaten/rohstoffe"},
        ////            { typeof(KundeDto), "/api/stammdaten/kunden"},
        ////            { typeof(AnfrageDto), "/api/anfragen"},
        ////            { typeof(VorkalkulationDto), "/api/vorkalkulationen"},
        ////            { typeof(LieferbedingungDto), "/api/stammdaten/lieferbedingungen"},
        ////            { typeof(ZahlungsbedingungDto), "/api/stammdaten/zahlungsbedingungen"},
        ////            { typeof(SavingDto), "/api/stammdaten/savings"},
        ////            { typeof(EinlegeteilDto), "/api/stammdaten/einlegeteile"},
        ////            { typeof(VerpackungDto), "/api/stammdaten/verpackungen"},
        ////            { typeof(WerkzeugtypDto), "/api/stammdaten/werkzeugtypen"},
        ////            { typeof(HandlingsgeraetDto), "/api/stammdaten/handlingsgeraete"},
        ////            { typeof(MaschineDto), "/api/stammdaten/maschinen"},
        ////            { typeof(PersonalDto), "/api/stammdaten/personal"},
        ////            { typeof(StandortDto), "/api/stammdaten/standorte"},
        ////            { typeof(NacharbeitsschrittDto), "/api/stammdaten/nacharbeitsschritte"},
        ////            { typeof(AngebotDto), "/api/angebote"},
        ////            { typeof(TextblockDto), "/api/stammdaten/textbloecke"},
        //        };

        protected override ApiResponse<T> Get<T>(string url)
        {
            var ret = base.Get<T>(url);
            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
            Assert.False(ret.HasError);
            Assert.NotNull(ret.Result);
            return ret;
        }

        protected override ApiResponse<IEnumerable<T>> GetAll<T>(string suffix = null)
        {
            var ret = base.GetAll<T>(suffix);
            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
            Assert.False(ret.HasError);
            Assert.NotNull(ret.Result);
            return ret;
        }

//        protected override ApiResponse<T> Post<T>(T content)
//        {
//            var ret = base.Post(content);
//            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
//            Assert.False(ret.HasError);
//            Assert.NotNull(ret.Result);
//            return ret;
//        }
//
//        protected override IEnumerable<ApiResponse<T>> PostMany<T>(IEnumerable<T> content)
//        {
//            var ret = base.PostMany(content);
//            foreach (var r in ret)
//            {
//                Assert.Equal(HttpStatusCode.OK, r.StatusCode);
//                Assert.False(r.HasError);
//                Assert.NotNull(r.Result);
//            }
//            return ret;
//        }
//
//        protected override ApiResponse<T> Put<T>(T content)
//        {
//            var ret = base.Put(content);
//            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
//            Assert.False(ret.HasError);
//            Assert.NotNull(ret.Result);
//            return ret;
//        }
//
//        protected override ApiResponse<T> Put<T>(int id, T content)
//        {
//            var ret = base.Put(id, content);
//            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
//            Assert.False(ret.HasError);
//            Assert.NotNull(ret.Result);
//            return ret;
//        }

        protected override ApiResponse<T> Put<T>(string uri, T content)
        {
            var ret = base.Put(uri, content);
            Assert.Equal(HttpStatusCode.OK, ret.StatusCode);
            Assert.False(ret.HasError);
            Assert.NotNull(ret.Result);
            return ret;
        }
    }
}
