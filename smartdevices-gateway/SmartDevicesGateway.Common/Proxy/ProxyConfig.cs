//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
namespace SmartDevicesGateway.Common.Proxy
{
    public class ProxyConfig
    {
        /// <summary>
        /// Change this to true to enable the proxy.
        /// </summary>
        public bool Disabled { get; set; }

        /// <summary>
        /// Change this to false to use specific proxy settings.
        /// </summary>
        public bool UseSystemDefaults { get; set; } = true;

        /// <summary>
        /// Change this to true, to bypass local web requests
        /// </summary>
        public bool BypassOnLocal { get; set; } = false;

        /// <summary>
        /// An array of regular expression strings that contains the URIs of the servers to bypass.
        /// </summary>
        public string[] BypassList { get; set; }

        /// <summary>
        /// Set this to true to enable user authentication (with username and password).
        /// </summary>
        public bool RequiresAuthentication { get; set; }

        /// <summary>
        /// Hostname of the proxy server.
        /// </summary>
        public string Hostname { get; set; }

        /// <summary>
        /// The port which the proxy is using. The default port is 8080.
        /// </summary>
        public int Port { get; set; } = 8080;

        /// <summary>
        /// Try to use credentials from the operating system (this may not work in some cases).
        /// To explicitly specify the username and password set this to false.
        /// </summary>
        public bool UseSystemCredentials { get; set; }

        /// <summary>
        /// Username for the proxy server.
        /// </summary>
        public string Username { get; set; }

        /// <summary>
        /// Password of the user for the proxy server.
        /// </summary>
        public string Password { get; set; }

        /// <summary>
        /// Domain of the web proxy. In some cases this can be left empty. 
        /// </summary>
        public string Domain { get; set; }

        public static ProxyConfig DefaultConfig => new ProxyConfig
        {
            Disabled = false,
            UseSystemDefaults = true,
            UseSystemCredentials = false,
            BypassOnLocal = false,
            BypassList = null,
            Hostname = "",
            Port = 8080,
            RequiresAuthentication = true,
            Username = "",
            Password = "",
            Domain = ""
        };
    }
}
