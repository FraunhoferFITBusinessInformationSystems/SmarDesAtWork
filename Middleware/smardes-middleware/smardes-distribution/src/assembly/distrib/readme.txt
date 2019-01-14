#-------------------------------------------------------------------------------
# Copyright (C) 2018-2019 camLine GmbH
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#-------------------------------------------------------------------------------
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
