# SmarDes Backend Service Configuration
## Directory Structure

After successful installation the backend configuration can be found in ...\smardes-middleware-x.y.z\smardes-backend-services-x.y.z

Inside there are the following sub-directories. In the following steps 
SMARDES_SERVICES_DIR denotes the base directory of the extracted 
distribution.

<table>
  <tr>
    <th>Directory</th>
    <th>Purpose</th>
  </tr>
  <tr>
    <td>bin</td>
    <td>Contains start and stop scripts for Linux and Windows. Moreover, support scripts to start the process as Windows service.</td>
  </tr>
  <tr>
    <td>config</td>
    <td>Configuration for the SmarDes backend services itself, e.g. rules, database queries, database monitoring configuration.</td>
  </tr>
  <tr>
    <td>db</td>
    <td>Contains the internal SmarDes database.</td>
  </tr>
  <tr>
    <td>externaldb</td>
    <td>Place to configure connections to external databases (also contains the required JDBC drivers)</td>
  </tr>
  <tr>
    <td>lib</td>
    <td>Required Java libraries</td>
  </tr>
  <tr>
    <td>licenses</td>
    <td>This folder contains license information about the third-party components in use.</td>
  </tr>
  <tr>
    <td>log</td>
    <td>Folder created on first startup. Contains the log files.</td>
  </tr>
  <tr>
    <td>maindata</td>
    <td>Contains main data as CSV or Excel files.
  </tr>
  <tr>
    <td>msgdump</td>
    <td>Created on demand. Contains files created with the dumpFile rule action.</td>
  </tr>
  <tr>
    <td>properties</td>
    <td>Contains configuration of third-party components.</td>
  </tr>
  <tr>
    <td>resources</td>
    <td>Created on startup. Contains temporary resources that are exchanged between smart devices.</td>
  </tr>
</table>
