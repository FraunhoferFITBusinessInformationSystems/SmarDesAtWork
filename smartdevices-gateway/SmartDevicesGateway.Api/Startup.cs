//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using log4net;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Http.Internal;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Serialization;
using SmartDevicesGateway.Api.EndPoints;
using SmartDevicesGateway.Api.HostedServices;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Common.Proxy;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Dto;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Processing.Controller.Config;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Handler;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.AuthService;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.FcmService;
using Swashbuckle.AspNetCore.Swagger;
using Vogler.Amqp;
using ErrorEventArgs = Newtonsoft.Json.Serialization.ErrorEventArgs;
using IApplicationLifetime = Microsoft.Extensions.Hosting.IApplicationLifetime;
using IHostingEnvironment = Microsoft.Extensions.Hosting.IHostingEnvironment;

namespace SmartDevicesGateway.Api
{
    public class Startup
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        private readonly ILoggerFactory _loggerFactory;
        private readonly IHostingEnvironment _hostingEnvironment;

        private IApplicationBuilder _app;
        private ILogger _logger;

        public IConfiguration Configuration { get; }
        public string ContentRoot { get; private set; }

        public Startup(IConfiguration configuration, ILoggerFactory loggerFactory, IHostingEnvironment hostingEnvironment)
        {
            Configuration = configuration;
            _loggerFactory = loggerFactory;
            _logger = loggerFactory.CreateLogger<Startup>();
            _hostingEnvironment = hostingEnvironment;

            ContentRoot = Configuration["contentRoot"] ?? ".";
        }

        public virtual void ConfigureServices(IServiceCollection services)
        {
            // CORS
            services.AddCors(o => o.AddPolicy("AllowAnyPolicy", builder =>
            {
                builder.AllowAnyOrigin()
                    .WithExposedHeaders(
                        "Accept-Ranges",
                        "Content-Encoding",
                        "Content-Length",
                        "Content-Range",
                        "X-Pagination",
                        "X-Rate-Limit-Limit",
                        "X-Rate-Limit-Remaining",
                        "X-Rate-Limit-Reset"
                    )
                    .AllowAnyMethod()
                    .AllowAnyHeader();
            }));

            var apiAssemblyName = Assembly.GetAssembly(typeof(Startup)).FullName;
            var modelAssemblyName = Assembly.GetAssembly(typeof(ServerInfo)).FullName;

            services.AddMvc()
                .AddJsonOptions(options =>
                {
                    options.SerializerSettings.ContractResolver = new CamelCasePropertyNamesContractResolver();
                    options.SerializerSettings.NullValueHandling = NullValueHandling.Ignore;
                    options.SerializerSettings.ReferenceLoopHandling = ReferenceLoopHandling.Ignore;
                    options.SerializerSettings.Error += OnJsonError;
                    options.SerializerSettings.Converters = new JsonConverter[]
                    {
                        new IsoDateTimeConverter
                        {
                            DateTimeFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.fffzzz"
                            //DateTimeFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss"
                        }
                    };
                })
                .AddApplicationPart(Assembly.Load(apiAssemblyName))
                .AddApplicationPart(Assembly.Load(modelAssemblyName));

            var apiDocXmlPath = $@"{ContentRoot}/API.xml";

            if (File.Exists(apiDocXmlPath))
            {
                Logger.Info($"Using API Documentation for Swagger from: {apiDocXmlPath}");
            }
            else
            {
                Logger.Error($"Could not find API Documentation for Swagger under: {apiDocXmlPath}");
            }
            services.AddSwaggerGen(c =>
            {
                c.SwaggerDoc("v1", new Info { Title = "SmartDevicesGatewayAPI", Version = "v1" });
                c.IncludeXmlComments(apiDocXmlPath);
                c.OperationFilter<BinaryBodyPayloadFilter>();
                c.OperationFilter<JsonBodyPayloadFilter>();
            });

            //Setup ConfigService
            var confService = new ConfigService(_loggerFactory) {ContentRoot = ContentRoot, Environment = _hostingEnvironment.EnvironmentName};

//            confService.RegisterConfigFile(Path.Combine(ContentRoot, "config/ServiceConfig.json"), typeof(ServiceConfig));
            confService.RegisterConfigFile(Path.Combine(ContentRoot, "config/SmartDeviceConfig.json"), typeof(SmartDeviceConfig));
            confService.RegisterConfigFile(Path.Combine(ContentRoot, "config/UserConfig.json"), typeof(UserConfig));
            confService.RegisterConfigFile(Path.Combine(ContentRoot, "config/AppConfig.json"), typeof(AppConfig));
            confService.RegisterConfigFile(Path.Combine(ContentRoot, "config/UiConfig.json"), typeof(UiConfig));

            services.AddSingleton<IConfigService>(confService);

            //Add other services
            //            var useAmqp = GetAmqpUseOrDefault(true);
            //            if (useAmqp)
            //            {
            //                services.AddSingleton<IAmqpService, AmqpService>();
            //            }
            //            else
            //            {
            //                services.AddSingleton<IAmqpService, NoAmqpService>();
            //            }
            //services.AddVoglerAmqpService(x => Configuration.GetSection(nameof(AmqpServiceConfig)).Bind(x));


            var connectionString = Configuration.GetValue<string>("AmqpServiceConfig:ConnectionString");
            var serviceName = Configuration.GetValue<string>("ServiceName");

            //Setup web proxy from config
            services.ConfigureWebProxy(options =>
                Configuration.GetSection(nameof(ProxyConfig)).Bind(options));

            // Add Vogler Amqp Service
            services.AddVoglerAmqpService(options =>
                {
                    options.ClientName = serviceName;
                    options.ConnectionString = connectionString;
                }
            );
            services.AddVoglerFcmService(options => 
                Configuration.GetSection(nameof(FcmServiceConfig)).Bind(options));

            services.AddSingleton<IAuthService, AuthService>();

            var db = new LiteDBPersistenceProvider(Path.Combine(ContentRoot, "SmartDevicesGateway.db"));

            services.AddSingleton<IPersistenceProvider>(db);

            //Add processing services
            services.AddSingleton<ConfigHandler>();
            services.AddSingleton<MessageHandler>();
            services.AddSingleton<ValueHandler>();
            services.AddTransient<DeviceInfoHandler>();
            services.AddSingleton<FcmMessageHandler>();
            services.AddSingleton<TodoListHandler>();

            //Add models as transient
            services.AddTransient<ConfigChangeModel>();
            services.AddTransient<AuthModel>();
            services.AddTransient<ConfigModel>();
            services.AddTransient<DataModel>();
            services.AddTransient<ActionModel>();
            services.AddTransient<JobModel>();
            services.AddTransient<ResourceModel>();
            services.AddTransient<TodoListModel>();
            services.AddTransient<ApkModel>();

            //Add Background Services
            services.AddSingleton<IHostedService, ConfigChangeListenerService>();
            services.AddSingleton<IHostedService, AmqpListenerService>();
        }
        
        private void OnJsonError(object sender, ErrorEventArgs errorEventArgs)
        {
            _logger.LogError(errorEventArgs?.ErrorContext?.Error?.Message);
            _logger.LogError($"JSON Parse Error at '{errorEventArgs?.ErrorContext?.Path}'");
        }

        public void Configure(IApplicationBuilder app, IHostingEnvironment env, ILoggerFactory loggerFactory, 
            IAmqpService amqpService, IApplicationLifetime applicationLifetime)
        {
            if (_hostingEnvironment.IsDevelopment() || _hostingEnvironment.IsEnvironment("DevelopmentServer"))
            {
                app.UseDeveloperExceptionPage();
            }

            loggerFactory.AddConsole(Configuration.GetSection("Logging"));

            //loggerFactory.AddDebug();
            loggerFactory.AddLog4Net(Path.Combine(ContentRoot, "config/log4net.config"));

            app.UseMvc();
            app.UseSwagger();
            app.UseSwaggerUI(c =>
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "SmartDevicesGatewayAPI");
            });

            //using (var serviceScope = app.ApplicationServices.GetRequiredService<IServiceScopeFactory>().CreateScope())
            //{
            //    var dbContext = serviceScope.ServiceProvider.GetService<SmartdevicesGatewayDBContext>();

            //    var dbNewlyCreated = dbContext.Database.EnsureCreated();
            //}

            applicationLifetime.ApplicationStarted.Register(OnStarted);
            applicationLifetime.ApplicationStopping.Register(OnShutdown);

            _app = app;
        }


        protected void OnStarted()
        {
            try
            {
                var fcmMessageHandler = _app?.ApplicationServices.GetRequiredService<FcmMessageHandler>();
                var persistenceProvider = _app?.ApplicationServices.GetRequiredService<IPersistenceProvider>();

                if (fcmMessageHandler == null || persistenceProvider == null)
                {
                    return;
                }

                var deviceIds = persistenceProvider.GetAllDeviceIds().ToArray();

                fcmMessageHandler.SendGetAllWithoutNotification(deviceIds);
            }
            catch (Exception e)
            {
                _logger.LogError("Error on StartUp.", e);
            }
        }

        protected void OnShutdown()
        {
        }
        
        public class AmqpOption
        {
            public bool UseAmqp { get; set; }
        }

        private AmqpOption AmqpOptions => (AmqpOption)(Configuration.GetSection("AmqpOption").Get(typeof(AmqpOption)));

        private bool GetAmqpUseOrDefault(bool @default)
        {
            var aqmpOption = AmqpOptions;
            if (aqmpOption?.UseAmqp == null)
            {
                return @default;
            }
            return aqmpOption.UseAmqp;
        }
    }

    public class LoggingMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<LoggingMiddleware> _logger;

        public LoggingMiddleware(RequestDelegate next, ILogger<LoggingMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }

        public async Task Invoke(HttpContext context)
        {
            using (var loggableResponseStream = new MemoryStream())
            {
                var originalResponseStream = context.Response.Body;
                context.Response.Body = loggableResponseStream;

                try
                {
                    // Log request
                    _logger.LogInformation(await FormatRequest(context.Request));

                    await _next(context);

                    // Log response
                    _logger.LogInformation(await FormatResponse(loggableResponseStream, context.Response.StatusCode));
                    //reset the stream position to 0
                    loggableResponseStream.Seek(0, SeekOrigin.Begin);
                    await loggableResponseStream.CopyToAsync(originalResponseStream);
                }
                catch (Exception ex)
                {
                    // Log error
                    _logger.LogError(ex, ex.Message);

                    //allows exception handling middleware to deal with things
                    throw;
                }
                finally
                {
                    //Reassign the original stream. If we are re-throwing an exception this is important as the exception handling middleware will need to write to the response stream.
                    context.Response.Body = originalResponseStream;
                }
            }
        }

        private static async Task<string> FormatRequest(HttpRequest request)
        {
            var body = request.Body;
            request.EnableRewind();
            var buffer = new byte[Convert.ToInt32(request.ContentLength)];
            await request.Body.ReadAsync(buffer, 0, buffer.Length);
            var bodyAsText = Encoding.UTF8.GetString(buffer);
            request.Body = body;

            var messageObjToLog = new { scheme = request.Scheme, host = request.Host, path = request.Path, queryString = request.Query, requestBody = bodyAsText };

            return JsonConvert.SerializeObject(messageObjToLog, Formatting.Indented);
        }

        private static async Task<string> FormatResponse(Stream loggableResponseStream, int statusCode)
        {
            var buffer = new byte[loggableResponseStream.Length];
            await loggableResponseStream.ReadAsync(buffer, 0, buffer.Length);

            var messageObjectToLog = new { responseBody = Encoding.UTF8.GetString(buffer), statusCode = statusCode };

            return JsonConvert.SerializeObject(messageObjectToLog);
        }
    }

    public static class StartupExtensions
    {
        public static IServiceCollection AddVoglerAmqpService(this IServiceCollection service, Action<AmqpServiceConfig> options)
        {
            var cfg = new AmqpServiceConfig();
            options.Invoke(cfg);

            service.AddSingleton(cfg);
            service.AddSingleton<IAmqpService, AmqpService>();
            return service;
        }

        public static IServiceCollection AddVoglerFcmService(this IServiceCollection service,
            Action<FcmServiceConfig> options)
        {
            var cfg = new FcmServiceConfig();
            options.Invoke(cfg);
            service.AddSingleton(cfg);
            service.AddSingleton<IFcmService>(ctx => new FcmService(ctx.GetService<FcmServiceConfig>(), ctx.GetService<ProxyConfig>()));
            //            service.AddSingleton<Func<IFcmService>>(ctx => () => new FcmService(ctx.GetService<FcmServiceConfig>()));
            return service;
        }

        public static IServiceCollection ConfigureWebProxy(this IServiceCollection service, Action<ProxyConfig> options)
        {
            var cfg = ProxyConfig.DefaultConfig;
            options.Invoke(cfg);
            service.AddSingleton(cfg);
            return service;
        }
    }
}
