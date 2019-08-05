# SmarDe's@Work

Public research project funded by the Bavarian Research Foundation

## Table of Contents

* [Installation](doc/installation.md)
* [Usage](doc/installation.md#Usage)
* [Documentation](doc/documentation.md)
  * [Architecture](doc/documentation.md#Architecture)
  * [Components](doc/documentation.md#Components)
    * [smardes-middleware](smardes-middleware/README.md)
    * [smartdevices-gateway](smartdevices-gateway/README.md)
    * [smartdevices-app](smartdevices-app/README.md)
  * [Interaction](doc/documentation.md#Interaction)
* Sample Configuration
  * UC1 MachineSetUp (TODO)
  * UC2 ToolBreakage (TODO)
  * [UC3 MaintenanceManual (TODO)](/doc/use-cases-config/uc3/install_uc3.md)
  * UC4 ErrorRecording (TODO)
  * [UC5 LiveData](/doc/use-cases-config/uc5/install_uc5.md)
  * [UC6 ValueMonitoring (with notification)](/doc/use-cases-config/uc6/install_uc6.md) 
* [Contributing](CONTRIBUTING.md)

## Description
Smart Devices (such as Smartphones and Smart Watches) offer many options to support production processes, such as optimization or worker assistance. So far, neither implementation recommendations nor standardized APIs exist. The project SmarDe´s@Work aims at usefully utilizing Smart Devices in the production area. In the foreground stands the connection of different production participants, such as workers, machines and information systems. The right combination of software and hardware through interactive and intuitive information processing supports the work preparation as well as the actual production and thereby generates efficiency along with raising optimization potentials. In order to ensure a broad applicability for different companies with different processes and IT-infrastructures, it would be required to integrate data from any information system such as ERP, MES or quality management. 

As a result, a central component of the project is the development of a __middleware__ combined with a __client application__ for different smart devices. This solution aims at __linking smart devices to the existing production systems__. This approach reduces integration hurdles of utilizing smart devices and ensures the transferability to varied use cases. The rule engine which can be configured individually by the user, ensures the applicability to a company-specific production system. In addition, the use of smart devices offers the option of a time and place independent production control. Production processes are digitally optimized, quality monitoring, control and guarantee based on real time date are implemented, and an Industry 4.0 required process flexibility reached.

# Funder

<img width="400" alt="Bayrische Forschungsstiftung" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/BayrischeForschungsstiftung.jpg"> 


# Consortium
In this repository the consortium consisting of two research partners, two development partners and four application partners offers the results of the research project as open source software including the middleware, the client application, the gateway and the PLC-wing (machine interface). 

## Research partners

<table>
<tr>
    <td><img width="200" alt="Project Group Business Information Systems Engineering of the Fraunhofer FIT" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Fraunhofer_FIT.svg?sanitize=true"></td>
    <td><img width="200" alt="Fraunhofer IPA Projektgruppe Regenerative Produktion " src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Fraunhofer_IPA.svg?sanitize=true"> </td>
</tr>
</table>

## Application partners

<p align="center">
<table border="0">
<tr>
    <td><img width="200" alt="Bayrische Kunststoffwerke" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/BKW.png"> </td>
    <td><img width="200" alt="biTTner" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/biTTner.jpg"> </td>
    <td><img width="200" alt="Dietz" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Dietz.svg?sanitize=true"></td>
    <td><img width="200" alt="Rehau" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Rehau.jpg"></td>
</tr>
</table> </p>

## Development partners

<p align ="center">
<table border="0">
<tr>
    <td><img width="200" style="border: 1px solid black" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Vogler.png"></td>
    <td><img width="200" alt="CamLine" src="https://raw.githubusercontent.com/FraunhoferFITBusinessInformationSystems/SmarDesAtWork/master/Other/Logos/Camline.png"></td>
</tr>
</table> </p>


# Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

# License
This OpenSource project is based on the [MIT License](https://opensource.org/licenses/MIT).

