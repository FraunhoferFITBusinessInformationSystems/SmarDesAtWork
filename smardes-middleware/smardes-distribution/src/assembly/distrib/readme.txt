Brief installation instructions
===============================

Run install.bat and follow the steps described there. The script performs the following steps:
- Create an Artemis broker
- Patch some files automatically
- (Interactive in Admin Command Prompt) Install the services
	* In %ARTEMIS_BROKER_DIR%\bin using
		service install
	* In smardes-backend-services-${project.version}\bin using
		service install
  as explained in install.bat command window.
  For upgrade, a
  	service uninstall
  might be needed before

Configure the backend modules
- SmarDes service configuration in directory "config"
- Configuration of external databases in directory "externaldb"
- Eventually change Artemis and logging configuration in directory "properties"

Start both Windows services

Check log files if startup succeeded without errors/exceptions
- Look for "WARN", "ERROR" and "Exception" in log files
