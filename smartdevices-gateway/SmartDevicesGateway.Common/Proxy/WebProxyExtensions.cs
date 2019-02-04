//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Net;
using System.Reflection;
using System.Text;
using log4net;

namespace SmartDevicesGateway.Common.Proxy
{
    public static class WebProxyExtensions
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public static HttpWebRequest SetProxyFromConfig(this HttpWebRequest request, ProxyConfig config)
        {
            //Reference:
            //https://docs.microsoft.com/en-us/dotnet/framework/network-programming/automatic-proxy-detection
            //request.Proxy = null; for no proxy
            //Default behaviour for WebRequests is to use application domain's default proxy in DefaultWebProxy property.
            //request.Proxy = WebRequest.DefaultWebProxy();

            request.Proxy = CreateWebProxyFromConfig(config);
            return request;
        }

        public static IWebProxy CreateWebProxyFromConfig(ProxyConfig config)
        {
            if (config.Disabled)
            {
                return null;
            }

            IWebProxy proxy = null;

            if (config.UseSystemDefaults)
            {
                proxy = WebRequest.GetSystemWebProxy();
                proxy.Credentials = CredentialCache.DefaultCredentials;

                return proxy;
            }

            Uri uri;
            try
            {
                uri = new Uri($"{config.Hostname}:{config.Port}");
            }
            catch (Exception e)
            {
                Logger.Warn("Could not read proxy server settings. Using no Proxy Server.", e);
                return null;
            }
            proxy = config.BypassList == null ? 
                    new WebProxy(uri, config.BypassOnLocal) : 
                    new WebProxy(uri, config.BypassOnLocal, config.BypassList);

            if (config.RequiresAuthentication)
            {
                if (config.UseSystemCredentials)
                {
                    proxy.Credentials = CredentialCache.DefaultCredentials;
                }
                else
                {
                    var credentials = new NetworkCredential(config.Username, config.Password, config.Domain);
                    proxy.Credentials = credentials;
                }
            }
            return proxy;
        }
    }
}
