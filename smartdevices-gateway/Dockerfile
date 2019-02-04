FROM microsoft/dotnet:2.1-runtime

WORKDIR /opt/smartdes-gateway
ADD SmartDevicesGateway.NetCoreService/bin/debug/netcoreapp2.0/publish/ ./

CMD [ "dotnet", "NetCoreService.dll", "--environment", "DevelopmentServer", "--console" ]
